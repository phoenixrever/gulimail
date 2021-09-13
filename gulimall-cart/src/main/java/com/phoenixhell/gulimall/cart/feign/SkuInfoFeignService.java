package com.phoenixhell.gulimall.cart.feign;

import com.phoenixhell.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;

@FeignClient("gulimall-product")
public interface SkuInfoFeignService {
    @RequestMapping("/product/skuinfo/info/{skuId}")
     R info(@PathVariable("skuId") Long skuId);

    //远程接口根据 skuid 返回销售属性
    @GetMapping("/product/skusaleattrvalue/sale/{skuId}")
     List<String> getSaleAttrValues(@PathVariable Long skuId);

    //远程获取商品价格
    @GetMapping("/product/skuinfo/{skuId}/price")
     BigDecimal getSkuPrice(@PathVariable Long skuId);
}
