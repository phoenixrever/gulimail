package com.phoenixhell.gulimall.ware.listener;


import com.phoenixhell.common.to.mq.OrderTo;
import com.phoenixhell.common.to.mq.StockLockedTo;
import com.phoenixhell.gulimall.ware.service.WareSkuService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@RabbitListener(queues ="stock.release.queue")
@Service
public class StockReleaseListener {
    @Autowired
    WareSkuService wareSkuService;

    /**
     * 库存自自动解锁
     *   解锁前根据订单号去订单系统查看订单状态
     *       1  到时间查看下订单状态 订单不存在  未支付状态 或者用户手动 取消状态  就解锁  并删除订单  其他状态都不解锁
     *       2  订单创建失败  到时间查询下这个订单 没有这个订单也解锁
     *
     *
     * 锁库存消息是在库存全部锁定后发送的  如果有一个失败会自动回滚 不会发送消息
     * 所有只要消息里面有此库存详情单 就一定存在 无需去数据库查询此库存详情单是否存在
     *
     *
     */

    @RabbitHandler
    void releaseLockStock(Message message, StockLockedTo stockLockedTo, Channel channel){
        System.out.println("收到库存解锁消息 库存解锁开始======>");
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            wareSkuService.releaseLockStock(stockLockedTo);
            channel.basicAck(deliveryTag,false);
        } catch (Exception e) {
            e.printStackTrace();
            //todo  requeue 不能为true 不然无限循环
            System.out.println("库存解锁失败======>"+e.getMessage());
            //远程查询失败  拒收此消息并重新入队 让别的库存服务来解锁 channel.basicReject() 功能一样 就是少个批量 这里不要批量
            try {
                channel.basicNack(deliveryTag,false,false);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @RabbitHandler
    void handleOrderClose(Message message, OrderTo orderTo, Channel channel){
        System.out.println("收到订单关闭消息 库存解锁开始======>");
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            wareSkuService.releaseLockStock(orderTo);
            channel.basicAck(deliveryTag,false);
        } catch (Exception e) {
            e.printStackTrace();
            //todo  requeue 不能为true 不然无限循环
            System.out.println("库存解锁失败======>"+e.getMessage());
            //远程查询失败  拒收此消息并重新入队 让别的库存服务来解锁 channel.basicReject() 功能一样 就是少个批量 这里不要批量
            try {
                channel.basicNack(deliveryTag,false,false);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
