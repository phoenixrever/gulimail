package com.phoenixhell.gulimall.seckill.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class MyRedissonConfig {

    /**
     * 所有对redisson的操作都要通过reidssonClient对象
     * 参数里面和autowired 一样可以取到yaml里面的值
     */
    @Bean(destroyMethod = "shutdown") //服务停止后会调用shutdown 方法进行销毁
    public RedissonClient redisson(@Value("spring.redis.host") String redisUrl) throws IOException {
        Config config = new Config();
        //可以用"rediss://"来启用SSL安全连接
        config.useSingleServer().setAddress("redis://"+redisUrl+":6379");
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }
}
