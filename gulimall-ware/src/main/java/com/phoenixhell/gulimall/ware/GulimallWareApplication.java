package com.phoenixhell.gulimall.ware;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

//有@Transaction注解会自动开启
//@EnableTransactionManagement
//指定扫描路径的包 这样可以扫到common里面的公共config
//@ComponentScan(basePackages = "com.phoenixhell")
@MapperScan(basePackages = "com.phoenixhell.gulimall.ware.dao")
@EnableDiscoveryClient
@EnableFeignClients
@SpringBootApplication
public class GulimallWareApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallWareApplication.class, args);
    }

}
