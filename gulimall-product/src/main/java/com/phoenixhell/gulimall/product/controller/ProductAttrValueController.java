package com.phoenixhell.gulimall.product.controller;

import com.phoenixhell.common.utils.PageUtils;
import com.phoenixhell.common.utils.R;
import com.phoenixhell.gulimall.product.entity.ProductAttrValueEntity;
import com.phoenixhell.gulimall.product.service.ProductAttrValueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;



/**
 * spu属性值
 *
 * @author phoenixhell
 * @email phoenixrever@gmail.com
 * @date 2021-05-13 23:21:46
 */
@RestController
@RequestMapping("product/productattrvalue")
public class ProductAttrValueController {
    @Autowired
    private ProductAttrValueService productAttrValueService;


    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = productAttrValueService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		ProductAttrValueEntity productAttrValue = productAttrValueService.getById(id);

        return R.ok().put("productAttrValue", productAttrValue);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody ProductAttrValueEntity productAttrValue){
		productAttrValueService.save(productAttrValue);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody ProductAttrValueEntity productAttrValue){
		productAttrValueService.updateById(productAttrValue);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		productAttrValueService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
