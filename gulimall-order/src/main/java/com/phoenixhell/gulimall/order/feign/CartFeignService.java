package com.phoenixhell.gulimall.order.feign;

import com.phoenixhell.gulimall.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient("gulimall-cart")
public interface CartFeignService {
    @GetMapping("/user/cartItem/checked")
     List<OrderItemVo> getCartItem();
}
