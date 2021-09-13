package com.phoenixhell.gulimall.ware.feign;

import com.phoenixhell.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("gulimall-order")
public interface OrderFeignService {
    @GetMapping("/order/order/status/{orderSn}")
     R getOrderStatus(@PathVariable String orderSn);
}
