package com.phoenixhell.gulimall.order.Interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;

//@Component
public class FeignInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
//        System.out.println("fegin 拦截器启动 interceptor=============2");
    }
}
