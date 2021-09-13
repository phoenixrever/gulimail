package com.phoenixhell.gulimall.product.feign;

import com.phoenixhell.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("gulimall-seckill")
public interface SeckillFeignService {
    @GetMapping("/seckill/info/{skuId}")
     R getSecKillInfoBySkuId(@PathVariable Long skuId);
}
