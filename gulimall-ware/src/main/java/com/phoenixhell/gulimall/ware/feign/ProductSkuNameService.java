package com.phoenixhell.gulimall.ware.feign;

import com.phoenixhell.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("gulimall-product")
public interface ProductSkuNameService {
    /**
     * /product/skuinfo/skuName/{skuId}  @FeignClient( gulimall-product)发请求
     * /api/product/skuinfo/skuName/{skuId} @FeignClient(  gulimall-gateway)发请求
     *
     */
    @RequestMapping("/product/skuinfo/info/{skuId}")
    public R info(@PathVariable("skuId") Long skuId);
}
