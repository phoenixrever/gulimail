package com.phoenixhell.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.phoenixhell.common.utils.PageUtils;
import com.phoenixhell.common.utils.Query;
import com.phoenixhell.gulimall.product.dao.ProductAttrValueDao;
import com.phoenixhell.gulimall.product.entity.AttrEntity;
import com.phoenixhell.gulimall.product.entity.ProductAttrValueEntity;
import com.phoenixhell.gulimall.product.service.AttrService;
import com.phoenixhell.gulimall.product.service.ProductAttrValueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Service("productAttrValueService")
public class ProductAttrValueServiceImpl extends ServiceImpl<ProductAttrValueDao, ProductAttrValueEntity> implements ProductAttrValueService {

    @Autowired
    private AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<ProductAttrValueEntity> page = this.page(
                new Query<ProductAttrValueEntity>().getPage(params),
                new QueryWrapper<ProductAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void updateSpuAttr(Long spuId, List<ProductAttrValueEntity> entities) {
        this.remove(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", spuId));
        List<ProductAttrValueEntity> collect = entities.stream().map(e -> {
            e.setSpuId(spuId);
            return e;
        }).collect(Collectors.toList());
        this.saveBatch(collect);
    }

    /**
     * 查询当前spu所有的可以被检索的(searchType=1)规格属性和其对应的值
     */
    @Override
    public  List<ProductAttrValueEntity> spuSearchableAttrs(Long spuId) {
        List<ProductAttrValueEntity> list = this.query().eq("spu_id", spuId).list();
        List<Long> ids = list.stream().map(item -> item.getAttrId()).collect(Collectors.toList());
        List<AttrEntity> attrEntities = attrService.listByIds(ids);
        //此次查询中 ProductAttrValue list  中可以被检索的attrId
        List<Long> searchableIds = attrEntities.stream().filter(attrEntity -> attrEntity.getSearchType() == 1).map(attrEntity -> attrEntity.getAttrId()).collect(Collectors.toList());

        //使用contains方法查询元素是否存在HashSet要比ArrayList快的多
        Set<Long> idSet= new HashSet<>(searchableIds);
        List<ProductAttrValueEntity> collect = list.stream().filter(item -> {
//            return searchableIds.contains(item.getAttrId());
            return idSet.contains(item.getAttrId());
        }).collect(Collectors.toList());

        return collect;
    }
}