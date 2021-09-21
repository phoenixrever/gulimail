package com.phoenixhell.gulimall.seckill.config;

import com.phoenixhell.gulimall.seckill.Interceptor.LoginUserInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Autowired
    LoginUserInterceptor loginUserInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //也可以再具体的拦截器配置拦截哪个
        registry.addInterceptor(loginUserInterceptor).addPathPatterns("/**");
    }
}
