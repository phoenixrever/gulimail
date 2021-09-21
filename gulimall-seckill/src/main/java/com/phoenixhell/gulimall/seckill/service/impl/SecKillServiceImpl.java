package com.phoenixhell.gulimall.seckill.service.impl;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.phoenixhell.common.to.mq.OrderItemEntityTo;
import com.phoenixhell.common.to.mq.SecKillTo;
import com.phoenixhell.common.utils.R;
import com.phoenixhell.common.vo.MemberVo;
import com.phoenixhell.gulimall.seckill.Interceptor.LoginUserInterceptor;
import com.phoenixhell.gulimall.seckill.To.SecKillSkuRedisTo;
import com.phoenixhell.gulimall.seckill.feign.CouponFeignService;
import com.phoenixhell.gulimall.seckill.feign.ProductFeignService;
import com.phoenixhell.gulimall.seckill.service.SecKillService;
import com.phoenixhell.gulimall.seckill.vo.SecKillSessionsWithSkusVo;
import com.phoenixhell.gulimall.seckill.vo.SkuInfoVo;
import com.sun.deploy.security.BlockedException;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class SecKillServiceImpl implements SecKillService {
    public final String SESSION_CACHE_PREFIX = "seckill:session:";
    public final String SKUKILL_CACHE_PREFIX = "seckill:skus:";
    public final String SKUSTOCK_SEMAPHORE = "seckill:stock:"; //后面接商品随机码

    @Autowired
    CouponFeignService couponFeignService;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    RedissonClient redissonClient;
    @Autowired
    private  RabbitTemplate rabbitTemplate;

    @Override
    public void upSecKillScheduledLast3Days() {
        //扫描最近3天需要参与秒杀活动的商品
        R r = couponFeignService.getLast3DaysSession();
        if (r.getCode() != 0) {
            return;
        }
        List<SecKillSessionsWithSkusVo> sessions = r.getData("sessions", new TypeReference<List<SecKillSessionsWithSkusVo>>() {
        });
        /**
         * redis 里面存储结构
         * 1 缓存活动信息 时间 --  商品skuId
         * 2 缓存活动关联的商品信息 商品skuId  对应的秒杀属性信息
         */
        if (sessions == null || sessions.size() == 0) {
            return;
        }
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        sessions.forEach(session -> {
            long startTime = session.getStartTime().getTime();
            long endTime = session.getEndTime().getTime();
            String key = SESSION_CACHE_PREFIX + startTime + "_" + endTime;
            BoundListOperations<String, String> listOperations = redisTemplate.boundListOps(key);
            List<String> range = listOperations.range(0, -1);
            if (session.getSeckillSkuRelationEntities().size() > 0) {
                //活动 商品 skuid
                session.getSeckillSkuRelationEntities().forEach(s -> {
                    if (!operations.hasKey(session.getId() + "_" + s.getSkuId())) {
                        //缓存活动关联的商品信息
                        SecKillSkuRedisTo secKillSkuRedisTo = new SecKillSkuRedisTo();
                        //1 秒杀信息
                        BeanUtils.copyProperties(s, secKillSkuRedisTo);
                        //2 sku商品信息
                        R info = productFeignService.getSkuInfo(s.getSkuId());
                        if (info.getCode() == 0) {
                            SkuInfoVo skuInfo = info.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                            });
                            //未关联的商品活动 不添加到redis
                            if (skuInfo == null) {
                                /*
                                 * 对于java8中的特殊写法lamada表达式中,不能使用break,会提示错误;
                                 * java8中使用return,会跳出当前循环,继续下一次循环,作用类似continue;
                                 * java8中使用foreach,但是不是lamada表达式写法,可以正常使用break或者return,可以直接跳出循环.
                                 */
                                return;
                            }
                            secKillSkuRedisTo.setSkuInfoVo(skuInfo);
                        }
                        secKillSkuRedisTo.setStartTime(startTime);
                        secKillSkuRedisTo.setEndTime(endTime);

                        // 秒杀随机码 防止构造post请求
                        String code = UUID.randomUUID().toString().substring(0, 6);
                        secKillSkuRedisTo.setRandomCode(code);

                        String jsonString = JSON.toJSONString(secKillSkuRedisTo);
                        //保存商品 场次活动id+ 商品skuId
                        operations.put(session.getId() + "_" + s.getSkuId(), jsonString);

                        //商品可以秒杀的库存作为信号量  限流  就是多线程抢资源
                        //等同于 7辆车抢3车位
                        if (!redisTemplate.hasKey(SKUSTOCK_SEMAPHORE + code)) {
                            RSemaphore semaphore = redissonClient.getSemaphore(SKUSTOCK_SEMAPHORE + code);
                            semaphore.trySetPermits(s.getSeckillCount());
                        }
                    }
                    //left push  现有数据的上面  right 现有数据的下面(尾部)
                    //缓存活动信息    //保存商品 场次活动id+ 商品skuId

                    boolean contains = range.contains(session.getId() + "_" + s.getSkuId());
                    if (!contains) {
                        listOperations.rightPush(session.getId() + "_" + s.getSkuId());
                    }
                });
            }

        });
    }

    public  List<SecKillSkuRedisTo>  annotationBlockHandler(BlockException exception){
        //必须传入这个异常  相当于catch 掉了 不然页面会报错
        System.out.println(exception);
        System.out.println("annotationBlockHandler  ===============================");
        return null;
    }


    //返回当前正在参与秒杀的商品
    //注解限流 blockHandler fallback的方法名字 注意 返回类型一定要一样
    //另外还能拿到原方法的参数 并且 会多加一个BlockException ex 参数
    //必须传入这个异常  相当于catch 掉了 不然页面会报错
    //@SentinelResource(value = "getKillSkus",blockHandler ="annotationBlockHandler" )
    @Override
    public List<SecKillSkuRedisTo> getCurrentSecKillSkus() {
        BoundHashOperations<String, String, String> operations = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        //先去秒杀场次查询符合当前场次的商品skuId   SESSION_CACHE_PREFIX = "seckill:session:";
        List<SecKillSkuRedisTo> secKillSkuRedisTos = new ArrayList<>();
        long time = System.currentTimeMillis();
        Set<String> keys = redisTemplate.keys(SESSION_CACHE_PREFIX + "*");
        //自定义sentinel保护资源
        try(Entry entry = SphU.entry("secKillSkus")) {
            if (keys != null && keys.size() > 0) {
                for (String key : keys) {
                    String timeString = key.replace(SESSION_CACHE_PREFIX, "");
                    String[] split = timeString.split("_");
                    //返回正在秒杀时间内的商品
                    if (time >= Long.parseLong(split[0]) && time <= Long.parseLong(split[1])) {
                        BoundListOperations<String, String> listOperations = redisTemplate.boundListOps(key);
                        List<String> range = listOperations.range(0, -1);
                        if (range != null && range.size() > 0) {
                            //range.forEach(item -> {
                            //    String o = operations.get(item);
                            //    if (o != null) {
                            //        SecKillSkuRedisTo secKillSkuRedisTo = JSON.parseObject(o.toString(), new TypeReference<SecKillSkuRedisTo>() {
                            //        });
                            //        //当前秒杀已经开始可以带上随机码
                            //        //secKillSkuRedisTo.setRandomCode(null);
                            //        secKillSkuRedisTos.add(secKillSkuRedisTo);
                            //    }
                            //});
                            // multiGet  提示 直接获取多个key的值  前提是声明绑定时候所有数据都为string
                            //BoundHashOperations<String, String, String> operations
                            List<String> multiGet = operations.multiGet(range);
                            if (!StringUtils.isEmpty(multiGet)) {
                                List<SecKillSkuRedisTo> skuRedisTos = JSON.parseObject(multiGet.toString(), new TypeReference<List<SecKillSkuRedisTo>>() {});
                                //当前秒杀已经开始可以带上随机码
                                secKillSkuRedisTos.addAll(skuRedisTos);
                            }
                        }
                        //当前场次符合的只有一个
                        //break;
                    }
                }
            }
        } catch (BlockedException | BlockException e) {
            e.printStackTrace();
            System.out.println("自定义资源保护中。。。。。。。。。。。");
        }
        return secKillSkuRedisTos;
    }

    @Override
    public SecKillSkuRedisTo getSecKillInfoBySkuId(Long skuId) {
        //找到所有参与秒杀的商品
        BoundHashOperations<String, String, String> operations = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        Set<String> keys = operations.keys();
        if(keys!=null && keys.size()>0){
            for (String key : keys) {
                String[] split = key.split("_");
                boolean b = split[1].equals(skuId.toString());
                if(b){
                    String s = operations.get(key);
                    SecKillSkuRedisTo secKillSkuRedisTo = JSON.parseObject(s, new TypeReference<SecKillSkuRedisTo>() {});
                    long currentTimeMillis = System.currentTimeMillis();
                    //超过秒杀时间就不算秒杀商品
                    if(secKillSkuRedisTo.getEndTime()<currentTimeMillis){
                        return null;
                    }
                    //不在秒杀时间不返回随机码
                    if(currentTimeMillis<secKillSkuRedisTo.getStartTime() || currentTimeMillis>secKillSkuRedisTo.getEndTime()){
                        secKillSkuRedisTo.setRandomCode(null);
                    }
                    return secKillSkuRedisTo;
                }
            }
        }
        return null;
    }

    @Override
    public SecKillTo kill(String sessionId, Integer num, String code) {
        long begin = System.currentTimeMillis();
        MemberVo memberVo = LoginUserInterceptor.threadLocal.get();
        String orderSn=null;
        //从redis 获取当前秒杀商品的详细信息
        BoundHashOperations<String, String, String> operations = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        String jsonString = operations.get(sessionId);
        SecKillSkuRedisTo secKillSkuRedisTo = JSON.parseObject(jsonString, new TypeReference<SecKillSkuRedisTo>() {});
        if(secKillSkuRedisTo==null){
            return null;
        }

        //校验合法性

        //是否正在秒杀时间
        Long startTime = secKillSkuRedisTo.getStartTime();
        Long endTime = secKillSkuRedisTo.getEndTime();
        long currentTime = System.currentTimeMillis();
        if(currentTime<startTime || currentTime>endTime){
            return null;
        }

        //随机码code 是否一致
        boolean equals = secKillSkuRedisTo.getRandomCode().equals(code);
        if(!equals){
            return null;
        }

        //校验购买数量是否超过秒杀库存 并且 校验是否超过个人购买限制
         if(num >secKillSkuRedisTo.getSeckillCount() || num >secKillSkuRedisTo.getSeckillLimit()){
            return null;
         }

        // 还有验证一个用户只能买一次 userid 存入redis 占位  用户id+sessionId
        String key="limit:"+memberVo.getId()+"_"+sessionId;
        //超过活动时间自动过期
        long expire=endTime-currentTime;
        Boolean setIfAbsent = redisTemplate.opsForValue().setIfAbsent(key,num.toString(),expire, TimeUnit.MILLISECONDS);
        if(!setIfAbsent){
            return null;
        }

        /*
         * 布式信号量 抢占 限流 (谁能买到商品
         *  //商品可以秒杀的库存作为信号量  限流  就是多线程抢资源
         *  //等同于 7辆车抢3车位
         *  if (!redisTemplate.hasKey(SKUSTOCK_SEMAPHORE + code))
         *  获取当初设置的信号量
         */

        RSemaphore semaphore = redissonClient.getSemaphore(SKUSTOCK_SEMAPHORE + code);
        //信号量acquire占位是阻塞的 会一直等 不能让用户等  tryAcquire 拿不到信号量 迅速返回
        SecKillTo secKillTo = new SecKillTo();
        try {
            //boolean tryAcquire = semaphore.tryAcquire(num, 100, TimeUnit.MILLISECONDS);
            boolean tryAcquire = semaphore.tryAcquire(num);
            if(tryAcquire){
                //抢到了商品 创建订单(发送mq 消息就行)
                 orderSn = IdWorker.getTimeId();
                secKillTo.setOrderSn(orderSn);
                secKillTo.setNum(num);
                secKillTo.setMemberId(memberVo.getId());
                secKillTo.setSeckillPrice(secKillSkuRedisTo.getSeckillPrice());
                secKillTo.setPromotionSessionId(secKillSkuRedisTo.getPromotionSessionId());
                secKillTo.setSkuId(secKillSkuRedisTo.getSkuId());
                OrderItemEntityTo orderItemEntityTo = new OrderItemEntityTo();

                SkuInfoVo skuInfoVo = secKillSkuRedisTo.getSkuInfoVo();
                orderItemEntityTo.setCategoryId(skuInfoVo.getCatalogId());
                orderItemEntityTo.setSkuName(skuInfoVo.getSkuName());
                orderItemEntityTo.setSkuPic(skuInfoVo.getSkuDefaultImg());
                orderItemEntityTo.setSkuPrice(secKillSkuRedisTo.getSeckillPrice());
                orderItemEntityTo.setSkuQuantity(num);
                secKillTo.setOrderItemEntityTo(orderItemEntityTo);
                rabbitTemplate.convertAndSend("order-event-exchange","order.seckill.kill",secKillTo);
                long end = System.currentTimeMillis();
                System.out.println("发送创建订单消息");
                System.out.println("一共耗时多少毫秒"+(end-begin));
            }
        } catch (Exception e) {
            //finally 先执行 return 时候 会检查下还有finally 没做完的事情
           return null;
        } finally {
            //semaphore.release(); 绝对不能释放信号量 这是秒杀 秒完了就完了
            //todo 可以设置几分钟没有支付的订单释放信号量
        }

        return secKillTo;
    }
}
