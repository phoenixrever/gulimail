package com.phoenixhell.gulimall.order.listener;

import com.phoenixhell.common.to.mq.SecKillTo;
import com.phoenixhell.gulimall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@RabbitListener(queues = "order.seckill.queue")
@Service  //决定不不要忘记注入容器
public class SecKillOrderListener {
    @Autowired
    OrderService orderService;

    @RabbitHandler
    public void  listen(Message message, SecKillTo secKillTo, Channel channel){
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            System.out.println("准备创建秒杀单的详细信息");
            orderService.createSecKillOrder(secKillTo);
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
