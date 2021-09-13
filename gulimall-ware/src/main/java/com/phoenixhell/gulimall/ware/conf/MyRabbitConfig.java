package com.phoenixhell.gulimall.ware.conf;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class MyRabbitConfig {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    //rabbitmq 的序列化机制 改成json
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

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
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                System.out.println("发送消息成功");
                //消息收到成功还是失败  消息被拒绝了 也是收到消息了 ack页数true
            }
        });

        //失败回调
        //服务器收到消息并且 如果消息没从exchange到达队列的回调 这是一个失败回调
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            /** 注意 消息没用到队列才会触发这个失败回调
             * @param message    投递失败的消息内容
             * @param replyCode  回复的状态码
             * @param replyText  回复的文本内容
             * @param exchange   消息发给的是哪个交换机
             * @param routingKey  消息发送时指定的路由键
             */
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                System.out.println("发送消息失败");
                //System.out.println("message====>" + message);
                //System.out.println("replyCode====>" + replyCode);
                //System.out.println("replyText====>" + replyText);
                //System.out.println("exchange====>" + exchange);
                //System.out.println("routingKey====>" + routingKey);
            }
        });

    }

    //给容器中放入 exchange 路由器与队列 queue 以及他们的绑定器
    //@bean  mq中没有这些组件时会自动创建 并注册到mq中
    //如果mq 中已经存在这些队列 即使属性方式改变 也不会重新创建

    //队列
    //死信队列 存放的消息都会过期  过期了向正常队列发送雄消息
    //延时队列:在rabbitmq中不存在延时队列,但是我们可以通过设置消息的过期时间和死信队列来模拟出延时队列
    @Bean
    public Queue stockDelayQueue() {
        //exclusive 排它  只服务一个消费者
        //durable  MQ服务器不丢数据（消息持久化 死机重启还在）
        //Queue(String name, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments)
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "stock-event-exchange");  //要发送的交换机
        arguments.put("x-dead-letter-routing-key", "stock.release.#");  //发送交换机带的路由键(指定要匹配的queue)
        arguments.put("x-message-ttl", 1000 * 30); //单位ms
        Queue stockDelayQueue = new Queue("stock.delay.queue", true, false, false, arguments);
        return stockDelayQueue;
    }

    //正常接受消息的队列
    @Bean
    public Queue stockReleaseQueue() {
        Queue stockReleaseQueue = new Queue("stock.release.queue", true, false, false);
        return stockReleaseQueue;
    }

    //库存服务默认交换机
    @Bean
    public Exchange stockEventExchange() {
        //TopicExchange(String name, boolean durable, boolean autoDelete, Map<String, Object> arguments)
        //autoDelete 没有消息监听对象会自动删除
        return new TopicExchange("stock-event-exchange", true, false);
    }

    @Bean
    public Binding stockLockedBinding() {
        // 目的地 与交换机绑定
        //Binding(String destination, DestinationType destinationType, String exchange, String routingKey,Map<String, Object> arguments)

        // public enum DestinationType {
        //		QUEUE,
        //		EXCHANGE;
        //	}
        return new Binding(
                "stock.delay.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.locked",
                null
        );
    }

    @Bean
    public Binding stockReleaseBinding() {
        return new Binding(
                "stock.release.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.release.#",
                null
        );
    }
}
