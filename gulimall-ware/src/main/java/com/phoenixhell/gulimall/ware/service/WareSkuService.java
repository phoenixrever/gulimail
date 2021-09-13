package com.phoenixhell.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.phoenixhell.common.to.mq.OrderTo;
import com.phoenixhell.common.to.mq.StockLockedTo;
import com.phoenixhell.common.utils.PageUtils;
import com.phoenixhell.gulimall.ware.entity.WareSkuEntity;
import com.phoenixhell.gulimall.ware.vo.SkuHasStockVo;
import com.phoenixhell.gulimall.ware.vo.WareSkuLockVo;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author phoenixhell
 * @email phoenixrever@gmail.com
 * @date 2021-05-19 21:24:08
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockVo> checkSkusHasStock(List<Long> skuIds);

    Boolean lockOrderStock(WareSkuLockVo vo);

    void releaseLockStock( StockLockedTo stockLockedTo) throws Exception;

    void releaseLockStock(OrderTo orderTo);
}

