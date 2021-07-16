package com.phoenixhell.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.phoenixhell.common.utils.PageUtils;
import com.phoenixhell.common.utils.Query;
import com.phoenixhell.gulimall.product.dao.CategoryBrandRelationDao;
import com.phoenixhell.gulimall.product.entity.BrandEntity;
import com.phoenixhell.gulimall.product.entity.CategoryBrandRelationEntity;
import com.phoenixhell.gulimall.product.entity.CategoryEntity;
import com.phoenixhell.gulimall.product.service.BrandService;
import com.phoenixhell.gulimall.product.service.CategoryBrandRelationService;
import com.phoenixhell.gulimall.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {

    @Autowired
    private CategoryService categoryService;
    @Autowired
    private BrandService brandService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                new QueryWrapper<CategoryBrandRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveDetail(CategoryBrandRelationEntity categoryBrandRelation) {
        //获取各自的name冗余数据存入关系表
        Long brandId = categoryBrandRelation.getBrandId();
        Long catalogId = categoryBrandRelation.getCatalogId();
        BrandEntity brandEntity = brandService.getById(brandId);
        CategoryEntity categoryEntity = categoryService.getById(catalogId);
        String brandName = brandEntity.getName();
        String categoryEntityName = categoryEntity.getName();
        categoryBrandRelation.setBrandName(brandName);
        categoryBrandRelation.setCatalogName(categoryEntityName);
        System.out.println(categoryBrandRelation);
        this.save(categoryBrandRelation);
    }

    @Override
    public Boolean updateBrand(Long brandId, String name) {
        CategoryBrandRelationEntity categoryBrandRelation = new CategoryBrandRelationEntity();
        categoryBrandRelation.setBrandName(name);
        categoryBrandRelation.setBrandId(brandId);
        return this.update(categoryBrandRelation, new UpdateWrapper<CategoryBrandRelationEntity>().eq("brand_id", brandId));
    }

    @Override
    public Boolean updateCategory(Long catId, String name) {
//        CategoryBrandRelationEntity CategoryBrandRelationEntity = new CategoryBrandRelationEntity();
//        CategoryBrandRelationEntity.setCatalogId(catId);
//        CategoryBrandRelationEntity.setCatalogName(name);
//        return this.update(CategoryBrandRelationEntity, new UpdateWrapper<CategoryBrandRelationEntity>().eq("catalog_id",catId));
          //换mapper写法
         return this.baseMapper.updateCategory(catId,name);

    }

}