package com.phoenixhell.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.phoenixhell.common.utils.PageUtils;
import com.phoenixhell.common.utils.Query;
import com.phoenixhell.gulimall.product.dao.BrandDao;
import com.phoenixhell.gulimall.product.entity.BrandEntity;
import com.phoenixhell.gulimall.product.service.BrandService;
import com.phoenixhell.gulimall.product.service.CategoryBrandRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Map;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<BrandEntity> queryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.eq("brand_id", key).or().like("name", key);
        }
        IPage<BrandEntity> page = this.page(
                new Query<BrandEntity>().getPage(params),
                queryWrapper);
        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void updateDetail(BrandEntity brand) {
        boolean updateById = this.updateById(brand);
        Boolean updateBrandRelation=true;
        //如果更新的数据中包含了name 因为name是category_brand_relation关系表中字段
        // 为保证冗余字段的数据一致需要更新category_brand_relation表
        if(!StringUtils.isEmpty(brand.getName())){
            //同步更新其他关联表中的数据
             updateBrandRelation = categoryBrandRelationService.updateBrand(brand.getBrandId(), brand.getName());
            //TODO 更新其他关联
        }
        if(!updateById || !updateBrandRelation){
            throw new RuntimeException("更新brand关系错误  事务回滚");
        }
    }

}