package com.phoenixhell.gulimall.order;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;


@EnableAspectJAutoProxy(exposeProxy=true) // 开启AspectJ的自动代理，同时要暴露代理对象
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 60*60*24*30)
@EnableRabbit
@EnableDiscoveryClient
@EnableFeignClients
@SpringBootApplication
public class GulimallOrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(GulimallOrderApplication.class, args);
    }
}
