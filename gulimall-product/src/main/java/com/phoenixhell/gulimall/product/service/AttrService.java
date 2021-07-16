package com.phoenixhell.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.phoenixhell.common.utils.PageUtils;
import com.phoenixhell.gulimall.product.entity.AttrEntity;
import com.phoenixhell.gulimall.product.vo.AttrRespVo;
import com.phoenixhell.gulimall.product.vo.AttrVo;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author phoenixhell
 * @email phoenixrever@gmail.com
 * @date 2021-05-13 23:21:46
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttr(AttrVo attrVo);

    PageUtils queryBaseAttrPage(Map<String, Object> params, Long catalogId, String attrType);


    AttrRespVo getAttrInfo(Long attrId);

    void updateAttr(AttrVo attrVo);


    List<AttrEntity> getRelationAttr(Long attrgroupId);
}

