package com.phoenixhell.gulimall.product.dao;

import com.phoenixhell.gulimall.product.entity.CategoryBrandRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 品牌分类关联
 * 
 * @author phoenixhell
 * @email phoenixrever@gmail.com
 * @date 2021-06-30 21:49:20
 */
@Mapper
public interface CategoryBrandRelationDao extends BaseMapper<CategoryBrandRelationEntity> {

    Boolean updateCategory(@Param("catId") Long catId,@Param("name") String name);
}
