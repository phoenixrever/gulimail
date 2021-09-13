package com.phoenixhell.gulimall.cart.config;

import com.phoenixhell.gulimall.cart.interceptor.CartInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//定制web的配置
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //拦截到购物车的所有请求
        registry.addInterceptor(new CartInterceptor()).addPathPatterns("/**");
        WebMvcConfigurer.super.addInterceptors(registry);
    }
}
