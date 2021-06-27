package com.phoenixhell.gulimall.product.dao;

import com.phoenixhell.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author phoenixhell
 * @email phoenixrever@gmail.com
 * @date 2021-05-13 23:21:46
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
