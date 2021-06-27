package com.phoenixhell.gulimall.thirdparty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class GuliMallThirdParty {
    public static void main(String[] args) {
        SpringApplication.run(GuliMallThirdParty.class, args);
    }
}
