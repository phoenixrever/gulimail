package com.phoenixhell.gulimall.product.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration
public class CookieSessionConfig {
    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        //默认
        serializer.setCookieName("JSESSIONID");

        serializer.setDomainName("gulimall.com");
        //cookie 存活时间 默认是session 浏览器一关就没  单位秒
        serializer.setCookieMaxAge(60*60*24*30);
        return serializer;
    }

    //spring session 以json 形式存入redis
    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        return new GenericJackson2JsonRedisSerializer();
    }
}
