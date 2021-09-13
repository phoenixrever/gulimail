package com.phoenixhell.gulimall.ware.dao;

import com.phoenixhell.gulimall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 商品库存
 * 
 * @author phoenixhell
 * @email phoenixrever@gmail.com
 * @date 2021-05-19 21:24:08
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    void addStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("skuNum") Integer skuNum);

    Long getSkuStock(@Param("skuId") Long skuId);

    Long lockStock(@Param("id") Long id, @Param("skuId") Long skuId, @Param("count") Integer count);

    void releaseLockStock(Long skuId, Long wareId, Integer count);
}
