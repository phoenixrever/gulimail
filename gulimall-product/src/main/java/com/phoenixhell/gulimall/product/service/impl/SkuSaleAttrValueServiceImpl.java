package com.phoenixhell.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.phoenixhell.common.utils.PageUtils;
import com.phoenixhell.common.utils.Query;
import com.phoenixhell.gulimall.product.dao.SkuSaleAttrValueDao;
import com.phoenixhell.gulimall.product.entity.SkuSaleAttrValueEntity;
import com.phoenixhell.gulimall.product.service.SkuSaleAttrValueService;
import com.phoenixhell.gulimall.product.vo.SkuItemVo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("skuSaleAttrValueService")
public class SkuSaleAttrValueServiceImpl extends ServiceImpl<SkuSaleAttrValueDao, SkuSaleAttrValueEntity> implements SkuSaleAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuSaleAttrValueEntity> page = this.page(
                new Query<SkuSaleAttrValueEntity>().getPage(params),
                new QueryWrapper<SkuSaleAttrValueEntity>()
        );
        return new PageUtils(page);
    }

    @Override
    public List<SkuItemVo.SkuItemSaleAttrVo> getSaleAttrsBySpuId(Long spuId) {
        List<SkuItemVo.SkuItemSaleAttrVo> skuItemSaleAttrVos =  baseMapper.getSaleAttrsBySpuId(spuId);
//        System.out.println(skuItemSaleAttrVos);
        return skuItemSaleAttrVos;
    }

}