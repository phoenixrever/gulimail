package com.phoenixhell.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.phoenixhell.common.utils.PageUtils;
import com.phoenixhell.gulimall.product.entity.BrandEntity;
import com.phoenixhell.gulimall.product.entity.CategoryBrandRelationEntity;

import java.util.List;
import java.util.Map;

/**
 * 品牌分类关联
 *
 * @author phoenixhell
 * @email phoenixrever@gmail.com
 * @date 2021-06-30 21:49:20
 */
public interface CategoryBrandRelationService extends IService<CategoryBrandRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveDetail(CategoryBrandRelationEntity categoryBrandRelation);

    Boolean updateBrand(Long brandId, String name);

    Boolean updateCategory(Long catId, String name);

    List<BrandEntity> getBrandsById(Long catId);
}

