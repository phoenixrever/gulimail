package com.phoenixhell.gulimall.cart.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

//ThreadPoolConfigProperties  加了@Component 就不需要这个注解
@EnableConfigurationProperties(ThreadPoolConfigProperties.class)
@Configuration
public class MyThreadPoolConfig {
    @Bean
    public ThreadPoolExecutor threadPoolExecutor(ThreadPoolConfigProperties pool) {
        /**
         *  cpu密集型任务
         *            最大线程数通常是逻辑处理器个数加1或者2  即12+1
         *
         *   IO 密集型任务 并不是在一直执行任务应该尽可能配置多的线程 如cpu核数*2
         *             cpu核数/阻塞系数
         *
         */
        //这边在外面配置文件动态配置更好
//        int processors = Runtime.getRuntime().availableProcessors();
//        System.out.println(Runtime.getRuntime().availableProcessors()+"个逻辑处理器");

        return new ThreadPoolExecutor(pool.getCorePoolSize(),
                pool.getMaximumPoolSize(),
                pool.getKeepAliveTime(),
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(pool.getWorkQueues()),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy()
        );
    }
}
