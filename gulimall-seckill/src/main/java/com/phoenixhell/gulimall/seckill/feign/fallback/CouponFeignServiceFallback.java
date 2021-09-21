package com.phoenixhell.gulimall.seckill.feign.fallback;

import com.phoenixhell.common.exception.BizCodeEnume;
import com.phoenixhell.common.utils.R;
import com.phoenixhell.gulimall.seckill.feign.CouponFeignService;
import org.springframework.stereotype.Component;


/**
 * feign 失败接口  实现远程feign接口并注入到容器种
 */
@Component
public class CouponFeignServiceFallback implements CouponFeignService {
    @Override
    public R getLast3DaysSession() {
        return R.error(BizCodeEnume.FEIGN_EXCEPTION.getCode(),BizCodeEnume.FEIGN_EXCEPTION.getMsg());
    }
}
