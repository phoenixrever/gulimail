package com.phoenixhell.gulimall.product.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.phoenixhell.gulimall.product.entity.AttrGroupEntity;
import com.phoenixhell.gulimall.product.vo.SkuItemVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 属性分组
 * 
 * @author phoenixhell
 * @email phoenixrever@gmail.com
 * @date 2021-05-13 23:21:46
 */
@Mapper
public interface AttrGroupDao extends BaseMapper<AttrGroupEntity> {

    List<SkuItemVo.SpuItemAttrGroupVo> getAttrGroupWithAttrBySpuId(Long spuId,Long catalogId);
}
