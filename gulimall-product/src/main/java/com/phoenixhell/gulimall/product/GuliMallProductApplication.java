package com.phoenixhell.gulimall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@MapperScan("com.phoenixhell.gulimall.product.dao")

// 假如feign 和main不在同一个包下需要指定包(会自动扫描main所在包下标注feignClient的类)
@EnableFeignClients(basePackages = "com.phoenixhell.gulimall.product.feign")
@EnableDiscoveryClient
@SpringBootApplication
public class GuliMallProductApplication {
    public static void main(String[] args) {
        SpringApplication.run(GuliMallProductApplication.class, args);
    }
}
