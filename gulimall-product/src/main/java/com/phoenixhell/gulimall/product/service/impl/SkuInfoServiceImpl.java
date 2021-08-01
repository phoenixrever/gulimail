package com.phoenixhell.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.phoenixhell.common.utils.PageUtils;
import com.phoenixhell.common.utils.Query;
import com.phoenixhell.gulimall.product.dao.SkuInfoDao;
import com.phoenixhell.gulimall.product.entity.SkuInfoEntity;
import com.phoenixhell.gulimall.product.service.SkuInfoService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Map;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> wrapper = new QueryWrapper<>();
        //key:
        //catalogId:
        //brandId:
        //min: 0
        //max: 0
        String key = (String) params.get("key");
        String minPrice = (String) params.get("min");
        String maxPrice = (String) params.get("max");
        String brandId = (String) params.get("brandId");
        String catalogId = (String) params.get("catalogId");

        if (!StringUtils.isEmpty(key)) {
            wrapper.and(item -> {
                item.eq("sku_id", key).or().like("sku_name", key);
            });
        }


        wrapper.ge(!StringUtils.isEmpty(minPrice), "price", minPrice);


        if (!StringUtils.isEmpty(maxPrice)) {
            try {
                BigDecimal bigDecimal = new BigDecimal(maxPrice);
                if (bigDecimal.compareTo(new BigDecimal(0)) == 1) {
                    wrapper.le("price", maxPrice);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }


        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            wrapper.eq("brand_id", brandId);
        }

        if (!StringUtils.isEmpty(catalogId) && !"0".equalsIgnoreCase(catalogId)) {
            wrapper.eq("catalog_id", catalogId);
        }
        IPage<SkuInfoEntity> page = this.page(new Query<SkuInfoEntity>().getPage(params), wrapper);

        return new PageUtils(page);
    }

}