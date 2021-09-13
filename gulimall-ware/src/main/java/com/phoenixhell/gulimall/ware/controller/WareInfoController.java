package com.phoenixhell.gulimall.ware.controller;

import com.phoenixhell.common.utils.PageUtils;
import com.phoenixhell.common.utils.R;
import com.phoenixhell.gulimall.ware.entity.WareInfoEntity;
import com.phoenixhell.gulimall.ware.service.WareInfoService;
import com.phoenixhell.gulimall.ware.vo.FareVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

/**
 * 仓库信息
 *
 * @author phoenixhell
 * @email phoenixrever@gmail.com
 * @date 2021-05-19 21:24:08
 */
@RestController
@RequestMapping("ware/wareinfo")
public class WareInfoController {
    @Autowired
    private WareInfoService wareInfoService;

    //获取运费信息 	fare 车费; 船费
    @GetMapping("/fare")
    public R getFare(@RequestParam("addressId") Long addressId){
        FareVo fareVo = wareInfoService.getFare(addressId);
        return R.ok().put("fareVo",fareVo);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = wareInfoService.queryPageByCondition(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        WareInfoEntity wareInfo = wareInfoService.getById(id);

        return R.ok().put("wareInfo", wareInfo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody WareInfoEntity wareInfo) {
        wareInfoService.save(wareInfo);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody WareInfoEntity wareInfo) {
        wareInfoService.updateById(wareInfo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        wareInfoService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
