package com.phoenixhell.gulimall.seckill.schduled;

import com.phoenixhell.gulimall.seckill.service.SecKillService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class SecKillScheduled {
    //分布式锁名字
    private final String SECKILL_UP_LOCK="seckill:up:";
    @Autowired
    RedissonClient redissonClient;
    @Autowired
    SecKillService secKillService;
    /**
     * 秒杀商品的定时上架
     * 每天晚上3点上架需要秒杀的商品
     * 当天 00:00:00 -  23:59:59
     * 明天 00:00:00 -  23:59:59
     * 后天 00:00:00 -  23:59:59
     *
     * 幂等性
     * 秒 分 时 日 月 周
     */
    @Async
    @Scheduled(cron = "0 0 0 * * ?")
    public void upSecKillScheduledLast3Days(){
        /*
         * 多个微服务最终只有1个微服务上架商品 解决 加分布式锁
         *
         */
        RLock lock = redissonClient.getLock(SECKILL_UP_LOCK);
        lock.lock(10, TimeUnit.SECONDS);
        try {
            System.out.println("上架的秒杀商品信息");
            secKillService.upSecKillScheduledLast3Days();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
}
