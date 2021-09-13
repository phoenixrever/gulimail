package com.phoenixhell.gulimall.seckill.feign;

import com.phoenixhell.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient("gulimall-coupon")
public interface CouponFeignService {
    @GetMapping("/coupon/seckillsession/last3DaysSession")
     R getLast3DaysSession();
}
