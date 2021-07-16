package com.phoenixhell.gulimall.product.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.phoenixhell.gulimall.product.entity.AttrAttrgroupRelationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 属性&属性分组关联
 * 
 * @author phoenixhell
 * @email phoenixrever@gmail.com
 * @date 2021-05-13 23:21:46
 */
@Mapper
public interface AttrAttrgroupRelationDao extends BaseMapper<AttrAttrgroupRelationEntity> {

    void deleteBatchRelationByIds(@Param("listEntities") List<AttrAttrgroupRelationEntity> listEntities);
}
