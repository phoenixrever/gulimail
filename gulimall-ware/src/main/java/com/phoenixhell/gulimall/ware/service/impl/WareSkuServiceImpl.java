package com.phoenixhell.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.phoenixhell.common.enume.OrderStatusEnum;
import com.phoenixhell.common.exception.BizCodeEnume;
import com.phoenixhell.common.exception.MyException;
import com.phoenixhell.common.to.mq.OrderTo;
import com.phoenixhell.common.to.mq.StockLockedTo;
import com.phoenixhell.common.to.mq.WareOrderTaskDetailTo;
import com.phoenixhell.common.utils.PageUtils;
import com.phoenixhell.common.utils.Query;
import com.phoenixhell.common.utils.R;
import com.phoenixhell.gulimall.ware.dao.WareSkuDao;
import com.phoenixhell.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.phoenixhell.gulimall.ware.entity.WareOrderTaskEntity;
import com.phoenixhell.gulimall.ware.entity.WareSkuEntity;
import com.phoenixhell.gulimall.ware.exception.NoStockException;
import com.phoenixhell.gulimall.ware.feign.OrderFeignService;
import com.phoenixhell.gulimall.ware.feign.ProductSkuNameService;
import com.phoenixhell.gulimall.ware.service.WareOrderTaskDetailService;
import com.phoenixhell.gulimall.ware.service.WareOrderTaskService;
import com.phoenixhell.gulimall.ware.service.WareSkuService;
import com.phoenixhell.gulimall.ware.vo.OrderItemVo;
import com.phoenixhell.gulimall.ware.vo.OrderVo;
import com.phoenixhell.gulimall.ware.vo.SkuHasStockVo;
import com.phoenixhell.gulimall.ware.vo.WareSkuLockVo;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {
    @Autowired
    ProductSkuNameService productSkuNameService;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    WareOrderTaskService wareOrderTaskService;
    @Autowired
    WareOrderTaskDetailService wareOrderTaskDetailService;
    @Autowired
    OrderFeignService orderFeignService;

    /**
     * 库存自自动解锁
     * 解锁前根据订单号去订单系统查看订单状态
     * 1  到时间查看下订单状态 订单不存在  未支付状态 或者用户手动 取消状态  就解锁  并删除订单  其他状态都不解锁
     * 2  订单创建失败  到时间查询下这个订单 没有这个订单也解锁
     * <p>
     * <p>
     * 锁库存消息是在库存全部锁定后发送的  如果有一个失败会自动回滚 不会发送消息
     * 所有只要消息里面有此库存详情单 就一定存在 无需去数据库查询此库存详情单是否存在
     */

    @Transactional
    @Override
    public void releaseLockStock(StockLockedTo stockLockedTo) throws Exception {
        System.out.println(stockLockedTo);
        //远程去order订单 服务查看订单状态
        WareOrderTaskEntity wareOrderTaskEntity = wareOrderTaskService.getById(stockLockedTo.getId());
        String orderSn = wareOrderTaskEntity.getOrderSn();
        R r = orderFeignService.getOrderStatus(orderSn);
        if (r.getCode() != 0) {
            throw new MyException(BizCodeEnume.FEIGN_EXCEPTION.getCode(), BizCodeEnume.FEIGN_EXCEPTION.getMsg());
        }
        OrderVo order = r.getData("order", new TypeReference<OrderVo>() {
        });
        System.out.println(order);
        //判断订单状态
        if (order == null || order.getStatus() == OrderStatusEnum.CANCLED.getCode()) {
            //如果工作单的状态改为1 就改为 2 已解锁
            if(wareOrderTaskEntity.getTaskStatus()==1){
                wareOrderTaskEntity.setTaskStatus(2);
            }
            wareOrderTaskService.updateById(wareOrderTaskEntity);
            List<WareOrderTaskDetailTo> details = stockLockedTo.getDetails();
            details.forEach(d -> {
                baseMapper.releaseLockStock(d.getSkuId(), d.getWareId(), d.getSkuNum());
                WareOrderTaskDetailEntity wareOrderTaskDetailEntity = new WareOrderTaskDetailEntity();
                wareOrderTaskDetailEntity.setLockStatus(2);
                wareOrderTaskDetailEntity.setId(d.getId());
                WareOrderTaskDetailEntity taskDetailEntity = wareOrderTaskDetailService.getById(d.getId());
                // 判断每一条订单项状态 如果为1 就改为2 1-锁定 2 解锁 3 扣除
                if (taskDetailEntity.getLockStatus() == 1) {
                    wareOrderTaskDetailService.updateById(wareOrderTaskDetailEntity);
                }
            });
        }
        System.out.println("库存解锁消息  解锁成功");
    }



    /**
     * 收到订单关闭消息解锁库存  防止解锁库存消息再订单关闭之前被消费
     * 库存只有订单是取消状态才会解锁  如果订单解锁在库存后面
     * 那么库存解锁先运行检查订单是new新键状态 就不会解锁了
     * 并且这条解锁库存的消息也消费掉了
     * 所以订单解锁后再发一次解锁库存消息，如果先前库存因为解锁消息快 查了数据库
     * 发现订单是new 状态  不会解锁库存并消费掉消息
     * 现在又收到了一条消息能保证解锁库存
     * 如果消息正常发送 库存解锁消息会发现 这条订单的库存已经是解锁status 也不会解锁
     */
    @Transactional
    @Override
    public void releaseLockStock(OrderTo orderTo) {
        //根据订单查询到库存工作单
        String orderSn = orderTo.getOrderSn();
        WareOrderTaskEntity orderTaskEntity = wareOrderTaskService.query()
                .eq("order_sn", orderSn)
                .eq("task_status",1)
                .one();
        //工作单存在 并且状态 1 锁定
        if (orderTaskEntity != null) {
            //工作单的状态改为 2 已解锁
            orderTaskEntity.setTaskStatus(2);
            wareOrderTaskService.updateById(orderTaskEntity);
            //处理工作项
            List<WareOrderTaskDetailEntity> detailEntities = wareOrderTaskDetailService.query().eq("task_id", orderTaskEntity.getId()).list();
            detailEntities.forEach(d -> {
                baseMapper.releaseLockStock(d.getSkuId(), d.getWareId(), d.getSkuNum());
                WareOrderTaskDetailEntity wareOrderTaskDetailEntity = new WareOrderTaskDetailEntity();
                wareOrderTaskDetailEntity.setLockStatus(2);
                wareOrderTaskDetailEntity.setId(d.getId());
                WareOrderTaskDetailEntity taskDetailEntity = wareOrderTaskDetailService.getById(d.getId());
                // 判断每一条订单项状态 如果为1 就改为2 1-锁定 2 解锁 3 扣除
                if (taskDetailEntity.getLockStatus() == 1) {
                    wareOrderTaskDetailService.updateById(wareOrderTaskDetailEntity);
                }
            });
        }
        System.out.println("订单关闭解锁消息  库存解锁成功");
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();
        String wareId = (String) params.get("wareId");
        String skuId = (String) params.get("skuId");
        wrapper.eq(!StringUtils.isEmpty(wareId), "ware_id", wareId);
        wrapper.eq(!StringUtils.isEmpty(skuId), "sku_id", skuId);

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params), wrapper);

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        //如果没有库存记录 新增
        List<WareSkuEntity> skuEntities = this.query().eq("sku_id", skuId).list();
        if (skuEntities.size() == 0) {
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setStockLocked(0);
            //远程查询sku_name 商品名称 失败整个事务不需要回滚
            //1catch掉异常
            //todo 其他方法异常不回滚
            try {
                R info = productSkuNameService.info(skuId);
                if (info.getCode() == 0) {
                    Map<String, Object> skuInfo = (Map<String, Object>) info.get("skuInfo");
                    wareSkuEntity.setSkuName((String) skuInfo.get("skuName"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.save(wareSkuEntity);
        } else {
            baseMapper.addStock(skuId, wareId, skuNum);
        }
    }

    @Override
    public List<SkuHasStockVo> checkSkusHasStock(List<Long> skuIds) {
        List<SkuHasStockVo> collect = skuIds.stream().map(skuId -> {
            //count有可能为null  SELECT SUM(stock-stock_locked)  库存量减去锁定量
            Long count = baseMapper.getSkuStock(skuId);
            //wareSkuDao 和baseMapper是一样的
            //System.out.println(baseMapper instanceof WareSkuDao);//true

            SkuHasStockVo skuHasStockVo = new SkuHasStockVo();
            skuHasStockVo.setSkuId(skuId);
            skuHasStockVo.setHasStock(count == null ? false : count > 0);
            return skuHasStockVo;
        }).collect(Collectors.toList());
        return collect;
    }

    //为某个订单锁定库存

    /**
     * 库存解锁的场景
     * 1 下订单成功 订单过期没有支付被系统自动取消 或者被用户手动取消
     * 2  下订单成功 库存锁定成功 但是接下来的业务失败 导致订单回滚
     * 之前锁定的库存就要自动解锁
     */
    //线程安全问题大家都来操作数据库 更新 mysql 有自己的隔离级别 update 是一个事务会加派它锁
    //(rollbackFor = NoStockException.class)
    //默认只要是运行时异常都会回滚
    @Transactional
    @Override
    public Boolean lockOrderStock(WareSkuLockVo vo) {
        List<OrderItemVo> locks = vo.getLocks();
        /**
         * 保存锁定库存的工作单为将来追溯与回滚做主准备
         * 先保存工作单就可以获取他的id
         */
        WareOrderTaskEntity wareOrderTaskEntity = new WareOrderTaskEntity();
        wareOrderTaskEntity.setOrderSn(vo.getOrderSn());
        //设置工单为锁定状态
        wareOrderTaskEntity.setTaskStatus(1);
        wareOrderTaskService.save(wareOrderTaskEntity);

        //按照下单的地址找到就近(这里查到谁有就是谁)仓库，锁定库存
        //找到每个商品在哪个仓库都要库存
        List<WareOrderTaskDetailEntity> list = new ArrayList<>();
        List<WareOrderTaskDetailTo> details = new ArrayList<>();
        boolean allLock = false;
        for (OrderItemVo item : locks) {
            Long skuId = item.getSkuId();
            List<WareSkuEntity> wareSkuEntities = this.query().eq("sku_id", skuId).list();
            if (wareSkuEntities == null || wareSkuEntities.size() == 0) {
                throw new NoStockException(skuId);
            }
            for (WareSkuEntity wareSkuEntity : wareSkuEntities) {
                //尝试锁定库存
                Long count = baseMapper.lockStock(wareSkuEntity.getId(), skuId, item.getCount());
                if (count > 0) {
                    //锁定其中一个购物项库存
                    allLock = true;
                    WareOrderTaskDetailEntity wareOrderTaskDetailEntity =
                            new WareOrderTaskDetailEntity(
                                    null,
                                    skuId,
                                    wareSkuEntity.getSkuName(),
                                    item.getCount(),
                                    wareOrderTaskEntity.getId(),
                                    wareSkuEntity.getWareId(),
                                    1); //1锁定成功
                    list.add(wareOrderTaskDetailEntity);
                    //不要在这里就把to赋值了 不保存成功里面没有id的
                    break;
                }else{
                    allLock=false;
                }
            }
            //如果其中一个商品没有锁成功 锁定失败抛出异常回滚
            if (!allLock) {
                throw new NoStockException(skuId);
            }
        }
        wareOrderTaskDetailService.saveBatch(list);
        //保存成功里面才有ID
        list.forEach(wareOrderTaskDetailEntity -> {
            WareOrderTaskDetailTo wareOrderTaskDetailTo = new WareOrderTaskDetailTo();
            BeanUtils.copyProperties(wareOrderTaskDetailEntity, wareOrderTaskDetailTo);
            details.add(wareOrderTaskDetailTo);
        });

        //mq 发送消息通知库存锁定成功
        StockLockedTo stockLockedTo = new StockLockedTo(wareOrderTaskEntity.getId(), details);
        rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", stockLockedTo);
        return allLock;
    }
}