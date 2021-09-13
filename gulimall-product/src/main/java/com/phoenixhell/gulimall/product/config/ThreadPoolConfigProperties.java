package com.phoenixhell.gulimall.product.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gulimall.thread")
//@Component 加了这个就不需要@EnableConfigurationProperties(ThreadPoolConfigProperties.class)
@Data
public class ThreadPoolConfigProperties {
    private Integer corePoolSize;
    int maximumPoolSize;
    long keepAliveTime;
    private Integer  workQueues;
}
