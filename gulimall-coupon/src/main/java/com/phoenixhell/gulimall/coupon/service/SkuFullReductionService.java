package com.phoenixhell.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.phoenixhell.common.to.SkuReductionTo;
import com.phoenixhell.common.utils.PageUtils;
import com.phoenixhell.gulimall.coupon.entity.SkuFullReductionEntity;

import java.util.Map;

/**
 * 商品满减信息
 *
 * @author phoenixhell
 * @email phoenixrever@gmail.com
 * @date 2021-05-18 21:59:07
 */
public interface SkuFullReductionService extends IService<SkuFullReductionEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSkuReduction(SkuReductionTo skuReductionTo);
}

