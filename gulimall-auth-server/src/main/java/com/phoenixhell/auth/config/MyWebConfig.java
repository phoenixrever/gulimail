package com.phoenixhell.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MyWebConfig implements WebMvcConfigurer {
    /**
     *     @GetMapping({"/login.html","/"})
     *     public String login(){
     *         return "login";
     *     }
     *     @GetMapping("/sign.html")
     *     public String sign(){
     *         return "sign";
     *     }
     */
    //视图映射  请求直接跳转的都可以在这定义 就不用写空方法
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/sign.html").setViewName("sign");
    }
}
