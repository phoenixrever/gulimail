package com.phoenixhell.gulimall.seckill.feign;

import com.phoenixhell.common.utils.R;
import com.phoenixhell.gulimall.seckill.feign.fallback.CouponFeignServiceFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(value = "gulimall-coupon",fallback = CouponFeignServiceFallback.class )
public interface CouponFeignService {
    @GetMapping("/coupon/seckillsession/last3DaysSession")
     R getLast3DaysSession();
}
