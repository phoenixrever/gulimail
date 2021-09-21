package com.phoenixhell.gulimall.product.feign.fallback;

import com.phoenixhell.common.exception.BizCodeEnume;
import com.phoenixhell.common.utils.R;
import com.phoenixhell.gulimall.product.feign.SeckillFeignService;
import org.springframework.stereotype.Component;

@Component
public class SeckillFeignServiceFallback implements SeckillFeignService {

    @Override
    public R getSecKillInfoBySkuId(Long skuId) {
        System.out.println("product 失败 回调");
        return R.error(BizCodeEnume.FEIGN_EXCEPTION.getCode(),BizCodeEnume.FEIGN_EXCEPTION.getMsg());
    }
}
