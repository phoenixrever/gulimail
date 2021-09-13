package com.phoenixhell.gulimall.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class MyRedissonConfig {
    /**
     * 所有对redisson的操作都要通过reidssonClient对象
     */
    @Bean(destroyMethod = "shutdown") //服务停止后会调用shutdown 方法进行销毁
    public RedissonClient redisson() throws IOException {
        Config config = new Config();
        //可以用"rediss://"来启用SSL安全连接
        config.useSingleServer().setAddress("redis://192.168.56.100:6379");
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }
}
