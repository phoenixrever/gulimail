package com.phoenixhell.gulimall.order;

import com.phoenixhell.gulimall.order.entity.OrderReturnReasonEntity;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

@SpringBootTest
public class TestRabbitMq {
    //AmqpAdmin 创建交换机
    @Autowired
    private AmqpAdmin amqpAdmin;
    // 发送消息
    @Autowired
    RabbitTemplate rabbitTemplate;

    //======================AmqpAdmin 创建交换机=========================
    @Test
    void createExchange() {
        //全参数构造器
        //public DirectExchange(String name, boolean durable, boolean autoDelete, Map<String, Object> arguments) {
        HashMap<String, Object> arguments = new HashMap<>();
        DirectExchange phoenixhellExchange = new DirectExchange(
                "phoenixhell-exchange",
                true,
                false,
                arguments
        );
        amqpAdmin.declareExchange(phoenixhellExchange);
        System.out.println(phoenixhellExchange);
    }

    @Test
    void createQueue() {
        //exclusive 排他 有人联上了其他人不能连 一般false
        //	public Queue(String name, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments) {
        //	public Queue(String name) {
        //		this(name, true, false, false);
        //	}
        String declareQueue = amqpAdmin.declareQueue(new Queue("phoenixhell-queue"));
        System.out.println(declareQueue);
    }

    @Test
    void binding() {
        // public enum DestinationType {
        //		QUEUE,
        //		EXCHANGE;
        //	}
        Binding binding = new Binding(
                "phoenixhell-queue",
                Binding.DestinationType.QUEUE,
                "phoenixhell-exchange",
                "phoenixhell-queue",
                null
        );
        amqpAdmin.declareBinding(binding);
    }

    //============================ 发送消息 ===========================
    @Test
    void sendMessage() {
        OrderReturnReasonEntity reason = new OrderReturnReasonEntity();
        reason.setName("退货名称");
        ZonedDateTime zonedDateTime = LocalDateTime.now().atZone(ZoneId.systemDefault());
        Date date = Date.from(zonedDateTime.toInstant());
        reason.setCreateTime(date);
        reason.setId(123456L);

        for (int i = 0; i < 20; i++) {
            if(i%2==0){
                rabbitTemplate.convertAndSend("phoenixhell-exchange", "phoenixhell-queue", reason,new CorrelationData(UUID.randomUUID().toString()));
            }else{
                rabbitTemplate.convertAndSend("phoenixhell-exchange", "phoenixhell-queue", "ssssssssssssss",new CorrelationData(UUID.randomUUID().toString()));
            }
        }
    }

    //手动接受消息
    @Test
    void  receiveMessage(){
        //接收到就会通确认删除消息  AutomaticAck
        OrderReturnReasonEntity orderReturnReasonEntity = (OrderReturnReasonEntity) rabbitTemplate.receiveAndConvert("phoenixhell-queue");
        System.out.println(orderReturnReasonEntity);
    }

    @Autowired
    StringRedisTemplate redisTemplate;
    @Test
    void testRedis(){

        Set<String> keys = redisTemplate.keys("spring:session:sessions:*");
        //Object[] array = keys.toArray();
        //System.out.println(array[0]);
        String[] split = keys.iterator().next().split(":");
        System.out.println(split[split.length-1]);
    }

}
