package com.phoenixhell.gulimall.product.controller;

import com.phoenixhell.common.utils.PageUtils;
import com.phoenixhell.common.utils.R;
import com.phoenixhell.common.valid.AddGroup;
import com.phoenixhell.common.valid.UpdateGroup;
import com.phoenixhell.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.phoenixhell.gulimall.product.entity.AttrEntity;
import com.phoenixhell.gulimall.product.entity.AttrGroupEntity;
import com.phoenixhell.gulimall.product.service.AttrAttrgroupRelationService;
import com.phoenixhell.gulimall.product.service.AttrGroupService;
import com.phoenixhell.gulimall.product.service.AttrService;
import com.phoenixhell.gulimall.product.service.CategoryService;
import com.phoenixhell.gulimall.product.vo.AttrGroupWithAttrsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;



/**
 * 属性分组
 *
 * @author phoenixhell
 * @email phoenixrever@gmail.com
 * @date 2021-05-13 23:21:46
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private AttrAttrgroupRelationService attrAttrgroupRelationService;

    /**
     *根据分组ID获取关联的所有基本属性
     */
    @GetMapping("/{attrgroupId}/attr/relation")
    public R attrRelation(@PathVariable Long attrgroupId){
        List<AttrEntity> entities = attrService.getRelationAttr(attrgroupId);
        return R.ok().put("data", entities);
    }

    /**
     *根据分类ID获取关联的所有分组及其各自的所有属性
     */
    @GetMapping("/{catalogId}/withattr")
    public R getAttrGroupWithAttr(@PathVariable Long catalogId){
        List<AttrGroupWithAttrsVo> entities = attrGroupService.getAttrGroupWithAttr(catalogId);
        return R.ok().put("data", entities);
    }

    /**
     *新增
     */
    @PostMapping("/attr/relation")
    public R addRelation(@RequestBody List<AttrAttrgroupRelationEntity> attrAttrgroupRelationEntities){
        attrAttrgroupRelationService.saveBatch(attrAttrgroupRelationEntities);
        return R.ok();
    }

    /**
     *根据分组ID获取没有关联的所有基本属性
     */
    @GetMapping("/{attrgroupId}/noattr/relation")
    public R attrNoRelation(@RequestParam Map<String, Object> params,@PathVariable Long attrgroupId){
        PageUtils page = attrService.getNoRelationAttr(params,attrgroupId);
        return R.ok().put("page", page);
    }



    /**
     *根据分关联数组删除所有属性关系对应
     * 接受json 数据要用@RequestBody
     */
    @PostMapping("/attr/relation/delete")
    public R attrRelation(@RequestBody List<AttrAttrgroupRelationEntity> listEntities){
        attrAttrgroupRelationService.deleteBatchRelationByIds(listEntities);
        return R.ok();
    }

    /**
     * 列表
     */
    @RequestMapping("/list/{catalogId}")
    public R list(@RequestParam Map<String, Object> params, @PathVariable Long catalogId){
//        PageUtils page = attrGroupService.queryPage(params);
        PageUtils page = attrGroupService.queryPage(params,catalogId);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        Long catalogId = attrGroup.getCatalogId();
        Long[] catalogPath = categoryService.findCatalogPath(catalogId);
        attrGroup.setCatalogPath(catalogPath);
        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@Validated(value = {AddGroup.class}) @RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@Validated(value = {UpdateGroup.class}) @RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}
