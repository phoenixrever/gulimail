package com.phoenixhell.gulimall.order.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Configuration
public class FeignConfig {
    @Bean
    public RequestInterceptor requestInterceptor(){
      return new RequestInterceptor() {
          @Override
          public void apply(RequestTemplate template) {
              //服务间的调用无需验证


              //RequestContextHolder(原理是threadLocal) 拿到浏览器发送的请求
              RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
              ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
            if(servletRequestAttributes != null){
                HttpServletRequest request = servletRequestAttributes.getRequest();
                //同步请求头数据（主要同步cookie）
                if(request!=null){
                    String cookie = request.getHeader("Cookie");
                    template.header("Cookie",cookie);
                }
            }
//              System.out.println("fegin 拦截器启动 config=============1");
          }
      };
    }
}
