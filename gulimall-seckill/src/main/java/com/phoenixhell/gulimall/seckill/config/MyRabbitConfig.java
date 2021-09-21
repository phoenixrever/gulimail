package com.phoenixhell.gulimall.seckill.config;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class MyRabbitConfig {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    //定制rabbitTemplate的回调 发送的消息被确认之后干什么
    //@PostConstruct  rabbitTemplate对象创建完成以后(应该是保证template对象存在)执行
    @PostConstruct
    public void initRabbitTemplate() {
        //服务器收到消息的回调
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            /**
             * 消息只要抵达 broker(服务器,代理) ack 回调(传回消息发送者)就是true
             * @param correlationData   #当前消息的唯一关联数据(消息的唯一id)
             * @param ack   #消息收到成功还是失败
             * @param cause  失败的原因
             *
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                System.out.println("发送消息成功");
                //消息收到成功还是失败  消息被拒绝了 也是收到消息了 ack也是true
                //
            }
        });

        //失败回调
        //服务器收到消息并且 如果消息没从exchange到达队列的回调 这是一个失败回调
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
                /** 注意 消息错误 没到达队列才会触发这个失败回调
             * @param message    投递失败的消息内容
             * @param replyCode  回复的状态码
             * @param replyText  回复的文本内容
             * @param exchange   消息发给的是哪个交换机
             * @param routingKey  消息发送时指定的路由键
             */
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                System.out.println("发送消息失败");
                //消息发送到队列失败  修改数据库把当前的消息状态修改为->错误
            }
        });

    }
}
