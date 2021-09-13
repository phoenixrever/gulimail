package com.phoenixhell.gulimall.order.feign;

import com.phoenixhell.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("gulimall-product")
public interface ProductFeignService {
    @GetMapping("/product/spuinfo/{skuId}")
     R getSpuBySkuId(@PathVariable Long skuId);
}
