package com.phoenixhell.gulimall.product.feign;

import com.phoenixhell.common.to.SkuReductionTo;
import com.phoenixhell.common.to.SpuBoundsTo;
import com.phoenixhell.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(value = "gulimall-coupon")
public interface CouponFeignService {

    //只要路径对了 方法名字随便改不一定要和复制过来的一样
    // 但是参数类型一定要一样 最好在要远程调用的那边在创建一个方法
    //专门给远程调用

    /**
     *
     *@RequestBody 将对象转成json 发送
     *
     */
    @RequestMapping("/coupon/spubounds/feign/saveSpuBounds")
    public R saveSpuBounds(@RequestBody SpuBoundsTo spuBoundsTo);

    @PostMapping("/coupon/skufullreduction/feign/saveSkuReduction")
    R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTo);
}

