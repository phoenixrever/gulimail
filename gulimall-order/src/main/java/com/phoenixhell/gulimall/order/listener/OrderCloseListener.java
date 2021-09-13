package com.phoenixhell.gulimall.order.listener;

import com.phoenixhell.gulimall.order.entity.OrderEntity;
import com.phoenixhell.gulimall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@RabbitListener(queues="order.release.queue")
@Service
public class OrderCloseListener {

    @Autowired
    OrderService orderService;

    @RabbitHandler
    public void  listen(Message message, OrderEntity entity, Channel channel){
        System.out.println("收到过期的订单信息 准备关闭订单==>"+entity);
        //不手动ack的话队列不会删除消息
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            //deliveryTag 当前通道内按照消息顺序自增的数字
            // multiple  是否批量确认  false 一个一个手动确认
            orderService.closeOrder(entity);
            channel.basicAck(deliveryTag,false);
        } catch (Exception e) {

            try {
                channel.basicReject(deliveryTag,false);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
