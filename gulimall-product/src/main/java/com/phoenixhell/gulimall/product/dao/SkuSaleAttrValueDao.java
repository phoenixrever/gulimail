package com.phoenixhell.gulimall.product.dao;

import com.phoenixhell.gulimall.product.entity.SkuSaleAttrValueEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.phoenixhell.gulimall.product.vo.SkuItemVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * sku销售属性&值
 * 
 * @author phoenixhell
 * @email phoenixrever@gmail.com
 * @date 2021-05-13 23:21:46
 */
@Mapper
public interface SkuSaleAttrValueDao extends BaseMapper<SkuSaleAttrValueEntity> {

    List<SkuItemVo.SkuItemSaleAttrVo> getSaleAttrsBySpuId(@Param("spuId") Long spuId);
}
