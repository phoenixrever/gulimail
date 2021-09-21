package com.phoenixhell.gulimall.product.feign;

import com.phoenixhell.common.utils.R;
import com.phoenixhell.gulimall.product.feign.fallback.SeckillFeignServiceFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "gulimall-seckill",fallback = SeckillFeignServiceFallback.class)
public interface SeckillFeignService {
    @GetMapping("/info/{skuId}")
     R getSecKillInfoBySkuId(@PathVariable Long skuId);
}
