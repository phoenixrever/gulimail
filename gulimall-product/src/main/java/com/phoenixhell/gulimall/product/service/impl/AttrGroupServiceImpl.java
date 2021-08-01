package com.phoenixhell.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.phoenixhell.common.utils.PageUtils;
import com.phoenixhell.common.utils.Query;
import com.phoenixhell.gulimall.product.dao.AttrGroupDao;
import com.phoenixhell.gulimall.product.entity.AttrEntity;
import com.phoenixhell.gulimall.product.entity.AttrGroupEntity;
import com.phoenixhell.gulimall.product.service.AttrAttrgroupRelationService;
import com.phoenixhell.gulimall.product.service.AttrGroupService;
import com.phoenixhell.gulimall.product.service.AttrService;
import com.phoenixhell.gulimall.product.vo.AttrGroupWithAttrsVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrAttrgroupRelationService attrAttrgroupRelationService;

    @Autowired
    private AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catalogId) {
        //如果分类ID为0 查询所有
        String key = (String) params.get("key");
        //select * from pms_attr_group where catalogId=? and (attr_group_id=key or attr_group_name like %key%)
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(key)) {
            wrapper.and(w -> {
                w.eq("attr_group_id", key).or().like("attr_group_name", key);
            });
        }

        if (catalogId != 0) {
            wrapper.eq("catalog_id", catalogId);
        }
        IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), wrapper);
        return new PageUtils(page);
    }

    @Override
    public List<AttrGroupWithAttrsVo> getAttrGroupWithAttr(Long catalogId) {
        //根据catalogID查出groupEntities
        List<AttrGroupEntity> groupEntities = this.query().eq("catalog_id", catalogId).list();

        List<AttrGroupWithAttrsVo> attrGroupWithAttrs = groupEntities.stream().map(groupEntity -> {
            AttrGroupWithAttrsVo attrGroupWithAttrsVo = new AttrGroupWithAttrsVo();
            BeanUtils.copyProperties(groupEntity, attrGroupWithAttrsVo);
            //getRelationAttr 实现了更具分组ID 查询所有的属性
            List<AttrEntity> groupWithAttr = attrService.getRelationAttr(groupEntity.getAttrGroupId());
            attrGroupWithAttrsVo.setAttrs(groupWithAttr);
            return attrGroupWithAttrsVo;
        }).collect(Collectors.toList());

        return attrGroupWithAttrs;
    }
}