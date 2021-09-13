package com.phoenixhell.gulimall.seckill.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync  //开启异步任务
@EnableScheduling
@Configuration
public class ScheduledConfig {
}
