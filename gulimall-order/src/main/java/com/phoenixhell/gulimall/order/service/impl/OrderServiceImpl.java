package com.phoenixhell.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.phoenixhell.common.constant.OrderConstant;
import com.phoenixhell.common.enume.OrderStatusEnum;
import com.phoenixhell.common.exception.BizCodeEnume;
import com.phoenixhell.common.exception.MyException;
import com.phoenixhell.common.to.SkuHasStockVo;
import com.phoenixhell.common.to.mq.OrderTo;
import com.phoenixhell.common.utils.PageUtils;
import com.phoenixhell.common.utils.Query;
import com.phoenixhell.common.utils.R;
import com.phoenixhell.common.vo.MemberVo;
import com.phoenixhell.gulimall.order.Interceptor.LoginUserInterceptor;
import com.phoenixhell.gulimall.order.vo.MyPage;
import com.phoenixhell.gulimall.order.dao.OrderDao;
import com.phoenixhell.gulimall.order.entity.OrderEntity;
import com.phoenixhell.gulimall.order.entity.OrderItemEntity;
import com.phoenixhell.gulimall.order.feign.CartFeignService;
import com.phoenixhell.gulimall.order.feign.MemberFeignService;
import com.phoenixhell.gulimall.order.feign.ProductFeignService;
import com.phoenixhell.gulimall.order.feign.WareFeginService;
import com.phoenixhell.gulimall.order.service.OrderItemService;
import com.phoenixhell.gulimall.order.service.OrderService;
import com.phoenixhell.gulimall.order.to.CreatedOrderTo;
import com.phoenixhell.gulimall.order.vo.*;
import lombok.SneakyThrows;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {
    private static final ThreadLocal<OrderSubmitVo> orderSubmitVoThreadLocal = new ThreadLocal<>();

    @Autowired
    private MemberFeignService memberFeignService;
    @Autowired
    private CartFeignService cartFeignService;
    @Autowired
    private WareFeginService wareFeginService;
    @Autowired
    private ThreadPoolExecutor executor;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ProductFeignService productFeignService;
    @Autowired
    private OrderItemService orderItemService;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );
        return new PageUtils(page);
    }

    //开启seata全局事务
    //@GlobalTransactional  高并发场景不适合用seata 采用消息队列
    //订单确认页需要的数据  此方法自身本地事务不能少
    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED)
    @Override
    public SubmitResponseVo submit(OrderSubmitVo orderSubmitVo) throws Exception {
        SubmitResponseVo responseVo = new SubmitResponseVo();
        responseVo.setCode(0);
        ThreadLocal<MemberVo> threadLocal = LoginUserInterceptor.threadLocal;
        MemberVo memberVo = threadLocal.get();
        orderSubmitVoThreadLocal.set(orderSubmitVo);
        String token = redisTemplate.opsForValue().get(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberVo.getId());

        //原子不安全
//        if(!StringUtils.isEmpty(token) && token.equals(orderSubmitVo.getOrderToken())){
//            //令牌验证通过 删掉令牌
//            redisTemplate.delete(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberVo.getId());
//        }else{
//
//        }

        //原子验证令牌和删除 lua 脚本 也可以用分布式锁redisson保证删除原子性
        //当然分布式式锁也是用lua实现的 这里就有点多此一举了
        //lua 返回 0  删除失败  1 删除成功  Long 类型
        String deleteLua = "if redis.call('get',KEYS[1])==ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        //参数1 脚本生成器 ，2 根据哪个key获取内容，3 获取的额内容和哪个值比对
        Long execute = redisTemplate.execute(new DefaultRedisScript<Long>(deleteLua, Long.class), Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberVo.getId()), token);

        if (execute == 0L) {
            //令牌验证失败
            throw new MyException(BizCodeEnume.TOKEN_NOT_MATCH_EXCEPTION.getCode(), BizCodeEnume.TOKEN_NOT_MATCH_EXCEPTION.getMsg());
        }
        //令牌验证成功 创建订单
        // 如果是requires_new  同一个service 下 必须调代理 不然还是继承required
        OrderServiceImpl orderService = (OrderServiceImpl) AopContext.currentProxy();
        CreatedOrderTo order = orderService.createOrder(memberVo);

        //验价格
        BigDecimal payPrice = order.getPayPrice();
        BigDecimal voPayment = orderSubmitVo.getPayment();
        double v = payPrice.subtract(voPayment).abs().doubleValue();
        if (v > 0.01) {
            //金额对比失败 请确认订单 失败事务回滚
            throw new MyException(BizCodeEnume.PRICE_NOT_MATCH_EXCEPTION.getCode(), BizCodeEnume.PRICE_NOT_MATCH_EXCEPTION.getMsg());
        }
        //保存订单数据
        saveOrder(order);

        // 锁库存 由异常抛出回滚数据
        WareSkuLockVo wareSkuLockVo = new WareSkuLockVo();
        wareSkuLockVo.setOrderSn(order.getOrder().getOrderSn());
        List<OrderItemVo> orderItemVos = order.getOrderItems().stream().map(item -> {
            OrderItemVo orderItemVo = new OrderItemVo();
            orderItemVo.setSkuId(item.getSkuId());
            orderItemVo.setCount(item.getSkuQuantity());
            return orderItemVo;
        }).collect(Collectors.toList());
        wareSkuLockVo.setLocks(orderItemVos);

        //高并发场景下 事务失败自身回滚同时发送消息队列 让监听此队列的库存服务回滚
        R r = wareFeginService.lockOrderStock(wareSkuLockVo);
        if (r.getCode() != 0) {
            //失败事务回滚
            throw new MyException(BizCodeEnume.NO_STOCK_EXCEPTION.getCode(), BizCodeEnume.NO_STOCK_EXCEPTION.getMsg());
        }
        //模拟积分服务失败
        //int num = 10 / 0;

        //订单创建成功 发消息给延时队列
        rabbitTemplate.convertAndSend("order-event-exchange","order.create.order",order.getOrder());

        responseVo.setOrder(order.getOrder());
        return responseVo;
    }

    @Override
    public void closeOrder(OrderEntity entity) {
        //查出当前订单状态
        OrderEntity orderEntity = this.getById(entity.getId());
        //0->待付款；1->待发货；2->已发货；3->已完成；4->已关闭；5->无效订单
        if(orderEntity.getStatus()==OrderStatusEnum.CREATE_NEW.getCode()){
            //关单
            OrderEntity order = new OrderEntity();
            order.setId(orderEntity.getId());
            order.setStatus(OrderStatusEnum.CANCLED.getCode());
            this.updateById(order);
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(orderEntity,orderTo);
            //解锁成功再给库存发一次消息
            //每一个发送的消息都要做好日志记录 (给数据库保存消息的每一个信息)
            //定期扫描数据库失败的消息再发一遍
            //todo  将发送消息写成中间件 微服务
            rabbitTemplate.convertAndSend("order-event-exchange","stock.release.#",orderTo);
        }
    }

    //查询当前登录用户的所有订单信息
    @Override
    public PageUtils queryListOrderItems(Map<String, Object> params) {
        MemberVo memberVo = LoginUserInterceptor.threadLocal.get();
        //为了方便可以直接再orderentity 对象里面添加orderItems 记得添加 tavlefield(exit=false) 声明不是数据库字段
        // 这里我不这样做  魔改一下返回的page就行 重写setRecords方法
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>().eq("member_id",memberVo.getId()).orderByDesc("id")
        );
        //老的分页属性全部复制过来
        MyPage<PageVo> myPage = new MyPage<>();
        BeanUtils.copyProperties(page,myPage);

        List<PageVo> pageVos = page.getRecords().stream().map(orderEntity -> {
            List<OrderItemEntity> orderItemEntities = orderItemService.query().eq("order_sn", orderEntity.getOrderSn()).list();
            PageVo pageVo = new PageVo();
            pageVo.setOrderEntity(orderEntity);
            pageVo.setOrderItemEntities(orderItemEntities);
            return pageVo;
        }).collect(Collectors.toList());

        myPage.setRecords(pageVos);
        return new PageUtils(myPage);
    }

    @SneakyThrows
    @Override
    public OrderConfirmVo confirmOrder() {
        ThreadLocal<MemberVo> threadLocal = LoginUserInterceptor.threadLocal;
        MemberVo memberVo = threadLocal.get();
        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();
        //得到主线程额上下文
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        //1 远程查询登录会员的所有收获地址列表    //todo 地址为空时前端提醒添加地址
        CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() -> {
            //将主线程的上下文存入副线程
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<MemberAddressVo> addressVos = memberFeignService.getAddress(memberVo.getId());
            orderConfirmVo.setAddressList(addressVos);
        }, executor);

        //2 远程查询购物车所有选中的购物项
        /**
         * Feign 在远程调用之前 要构造请求  会调用很多的拦截器 requestInterceptors
         *
         */
        CompletableFuture<Void> cartItemsFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemVo> cartItems = cartFeignService.getCartItem();
            if (cartItems != null && cartItems.size() > 0) {
                orderConfirmVo.setItems(cartItems);
                BigDecimal total = cartItems.stream()
                        .map(item -> item.getPrice().multiply(new BigDecimal(item.getCount().toString())))
                        .reduce(BigDecimal.ZERO, (preValue, CurrentValue) -> preValue.add(CurrentValue));
                orderConfirmVo.setTotal(total);

                //3 查询用户积分
                Integer integration = memberVo.getIntegration();
                orderConfirmVo.setIntegration(integration);
                orderConfirmVo.setPayment(total);
            } else {
                //todo提醒购物车没有货物请添加
            }
        }, executor).thenRunAsync(() -> {
            //远程查询库存系统
            List<OrderItemVo> items = orderConfirmVo.getItems();
            List<Long> skuIds = items.stream().map(item -> item.getSkuId()).collect(Collectors.toList());
            R r = wareFeginService.checkStock(skuIds);
            if (r.getCode() == 0) {
                List<SkuHasStockVo> skuHasStockVos = r.getData(new TypeReference<List<SkuHasStockVo>>() {
                });
                for (OrderItemVo item : items) {
                    for (SkuHasStockVo vo : skuHasStockVos) {
                        if (vo.getSkuId() == item.getSkuId()) {
                            if (vo.getHasStock()) {
                                item.setHasStock(vo.getHasStock());
                                break;
                            }
                        }
                    }
                }
                //方法二 直接在 orderConfirmVo 中加上这个map 前端 直接orderConfirmVo.map[item.skuId] 取出来
                //Map<Long, Boolean> map = skuHasStockVos.stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::getHasStock));
                orderConfirmVo.setItems(items);
            }
        }, executor);

        //防重令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        // 给前端一个
        orderConfirmVo.setOrderToken(token);
        //给服务器一个
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberVo.getId(), token, 30, TimeUnit.MINUTES);

        CompletableFuture<Void> allOf = CompletableFuture.allOf(addressFuture, cartItemsFuture);
        allOf.get();
        return orderConfirmVo;
    }
    //提交订单
    //事务代理具体说明见文档

    //保存订单数据
    private void saveOrder(CreatedOrderTo createdOrderTo) {
        OrderEntity order = createdOrderTo.getOrder();
        order.setModifyTime(new Date());
        this.save(order);
        List<OrderItemEntity> orderItems = createdOrderTo.getOrderItems();
        //seata不支持批量 垃圾 不用
        //orderItems.forEach(orderItemEntity -> {
        //    orderItemService.save(orderItemEntity);
        //});
        orderItemService.saveBatch(orderItems);
    }

    //创建订单方法
    //@Transactional  如果是required 可以省略  requires_new 不能省略
    //注意采用代理此方法决定不能是private 不然里面feign会失败
    CreatedOrderTo createOrder(MemberVo memberVo) {
        CreatedOrderTo createdOrderTo = new CreatedOrderTo();
        //1 生成订单号
        String orderSn = IdWorker.getTimeId();

        //2构建订单
        OrderEntity orderEntity = buildOrder(orderSn);
        orderEntity.setMemberId(memberVo.getId());
        orderEntity.setMemberUsername(memberVo.getUsername());
        //获取到所有订单项 获取当前购物车的订单项
        List<OrderItemEntity> orderItemEntities = buildOrderItems(orderSn);
        computePrice(orderEntity, orderItemEntities);

        createdOrderTo.setOrder(orderEntity);
        createdOrderTo.setOrderItems(orderItemEntities);
        createdOrderTo.setFare(orderEntity.getFreightAmount());
        createdOrderTo.setPayPrice(orderEntity.getPayAmount());
        return createdOrderTo;
    }

    //计算价格相关 总价格+运费金额
    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> orderItemEntities) {
        //BigDecimal totalPrice = orderItemEntities.stream().map(item -> item.getRealAmount()).reduce(BigDecimal.ZERO, (pre, current) -> pre.add(current));
        BigDecimal totalPrice = BigDecimal.ZERO;
        BigDecimal couponAmount = BigDecimal.ZERO;
        BigDecimal promotionAmount = BigDecimal.ZERO;
        BigDecimal integrationAmount = BigDecimal.ZERO;
        int giftIntegration = 0;
        int giftGrowth = 0;
        for (OrderItemEntity item : orderItemEntities) {
            totalPrice = totalPrice.add(item.getRealAmount());
            //各个种类 优惠的价格汇总
            couponAmount = couponAmount.add(item.getCouponAmount());
            promotionAmount = promotionAmount.add(item.getPromotionAmount());
            integrationAmount = integrationAmount.add(item.getIntegrationAmount());

            //每个商品能获得的积分  成长值汇总
            giftIntegration = giftIntegration + item.getGiftIntegration();
            giftGrowth = giftGrowth + item.getGiftGrowth();
            orderEntity.setIntegration(giftIntegration);
            orderEntity.setGrowth(giftGrowth);
        }
        //订单总额
        orderEntity.setTotalAmount(totalPrice);
        //应付总额
        orderEntity.setPayAmount(totalPrice.add(orderEntity.getFreightAmount()));
        //促销优化金额（促销价、满减、阶梯价）
        orderEntity.setPromotionAmount(promotionAmount);
        orderEntity.setCouponAmount(couponAmount);
        orderEntity.setIntegrationAmount(integrationAmount);
    }

    //构建订单
    private OrderEntity buildOrder(String orderSn) {
        OrderSubmitVo orderSubmitVo = orderSubmitVoThreadLocal.get();
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(orderSn);

        //获取收获信息feign 失败错误同一处理
        R r = wareFeginService.getFare(orderSubmitVo.getAddressId());
        FareVo fareVo = r.getData("fareVo", new TypeReference<FareVo>() {
        });
        //获取运费
        orderEntity.setFreightAmount(fareVo.getFare());
        //收货人信息
        orderEntity.setReceiverName(fareVo.getMemberAddressVo().getName());
        orderEntity.setReceiverPhone(fareVo.getMemberAddressVo().getPhone());
        orderEntity.setReceiverCity(fareVo.getMemberAddressVo().getCity());
        orderEntity.setReceiverDetailAddress(fareVo.getMemberAddressVo().getDetailAddress());
        orderEntity.setReceiverPostCode(fareVo.getMemberAddressVo().getPostCode());
        orderEntity.setReceiverProvince(fareVo.getMemberAddressVo().getProvince());
        orderEntity.setReceiverRegion(fareVo.getMemberAddressVo().getRegion());

        //设置订单状态信息
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        //自动确认时间(默认收获)
        orderEntity.setAutoConfirmDay(7);
        //默认删除时间
        orderEntity.setDeleteStatus(0);
        return orderEntity;
    }

    //构建订单项
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        //此feign调用最后一次确定订单的价格
        List<OrderItemVo> cartItems = cartFeignService.getCartItem();
        if (cartItems != null && cartItems.size() > 0) {
            List<OrderItemEntity> orderItemEntities = cartItems.stream().map(item -> {
                OrderItemEntity orderItemEntity = buildOrderItem(item);
                //1 订单信息 订单号
                orderItemEntity.setOrderSn(orderSn);
                return orderItemEntity;
            }).collect(Collectors.toList());
            return orderItemEntities;
        }
        return null;
    }

    //每个订单项
    private OrderItemEntity buildOrderItem(OrderItemVo item) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        //2 商品的spu信息
        Long skuId = item.getSkuId();
        R r = productFeignService.getSpuBySkuId(skuId);
        SpuInfoVo spuInfo = r.getData("spuInfo", new TypeReference<SpuInfoVo>() {
        });
        orderItemEntity.setSpuId(spuInfo.getId());
        orderItemEntity.setSpuBrand(spuInfo.getSpuName());
        orderItemEntity.setCategoryId(spuInfo.getCatalogId());
        //3 商品的sku信息
        orderItemEntity.setSkuId(item.getSkuId());
        orderItemEntity.setSkuName(item.getTitle());
        orderItemEntity.setSkuPic(item.getDefaultImg());
        orderItemEntity.setSkuPrice(item.getPrice());
        orderItemEntity.setSkuQuantity(item.getCount());
        orderItemEntity.setSkuAttrsVals(String.join("-", item.getSkuAttr()));

        //4 优惠信息 不做

        //5 积分
        orderItemEntity.setGiftGrowth(item.getPrice().multiply(BigDecimal.valueOf(item.getCount())).intValue());
        orderItemEntity.setGiftIntegration(item.getPrice().multiply(new BigDecimal(item.getCount())).intValue());

        //验价 计算价格相关
        orderItemEntity.setPromotionAmount(BigDecimal.ZERO);
        orderItemEntity.setCouponAmount(BigDecimal.ZERO);
        orderItemEntity.setIntegrationAmount(BigDecimal.ZERO);
        //当前订单项目的实际金额 总额-优惠
        BigDecimal originPrice = orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity()));
        BigDecimal realAmount = originPrice.subtract(orderItemEntity.getPromotionAmount()).subtract(orderItemEntity.getCouponAmount()).subtract(orderItemEntity.getIntegrationAmount());
        orderItemEntity.setRealAmount(realAmount);

        return orderItemEntity;
    }


}