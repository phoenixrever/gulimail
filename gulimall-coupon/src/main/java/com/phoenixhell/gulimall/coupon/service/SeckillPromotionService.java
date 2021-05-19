package com.phoenixhell.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.phoenixhell.common.utils.PageUtils;
import com.phoenixhell.gulimall.coupon.entity.SeckillPromotionEntity;

import java.util.Map;

/**
 * 秒杀活动
 *
 * @author phoenixhell
 * @email phoenixrever@gmail.com
 * @date 2021-05-18 21:59:07
 */
public interface SeckillPromotionService extends IService<SeckillPromotionEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

