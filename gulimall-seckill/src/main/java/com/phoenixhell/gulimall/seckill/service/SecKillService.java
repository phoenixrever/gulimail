package com.phoenixhell.gulimall.seckill.service;

import com.phoenixhell.common.to.mq.SecKillTo;
import com.phoenixhell.gulimall.seckill.To.SecKillSkuRedisTo;

import java.util.List;

public interface SecKillService {
    void upSecKillScheduledLast3Days();

    List<SecKillSkuRedisTo> getCurrentSecKillSkus();

    SecKillSkuRedisTo getSecKillInfoBySkuId(Long skuId);

    SecKillTo kill(String sessionId, Integer num, String code);
}
