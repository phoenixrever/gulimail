package com.phoenixhell.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.phoenixhell.common.utils.PageUtils;
import com.phoenixhell.gulimall.product.entity.AttrGroupEntity;
import com.phoenixhell.gulimall.product.vo.AttrGroupWithAttrsVo;
import com.phoenixhell.gulimall.product.vo.SkuItemVo;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author phoenixhell
 * @email phoenixrever@gmail.com
 * @date 2021-05-13 23:21:46
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPage(Map<String, Object> params, Long catalogId);

    List<AttrGroupWithAttrsVo> getAttrGroupWithAttr(Long catalogId);

    List<SkuItemVo.SpuItemAttrGroupVo> getAttrGroupWithAttrBySpuId(Long spuId,Long catalogId);
}

