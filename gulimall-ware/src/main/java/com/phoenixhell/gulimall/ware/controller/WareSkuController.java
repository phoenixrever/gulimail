package com.phoenixhell.gulimall.ware.controller;

import com.phoenixhell.common.exception.BizCodeEnume;
import com.phoenixhell.common.utils.PageUtils;
import com.phoenixhell.common.utils.R;
import com.phoenixhell.gulimall.ware.entity.WareSkuEntity;
import com.phoenixhell.gulimall.ware.exception.NoStockException;
import com.phoenixhell.gulimall.ware.service.WareSkuService;
import com.phoenixhell.gulimall.ware.vo.SkuHasStockVo;
import com.phoenixhell.gulimall.ware.vo.WareSkuLockVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * 商品库存
 *
 * @author phoenixhell
 * @email phoenixrever@gmail.com
 * @date 2021-05-19 21:24:08
 */
@RestController
@RequestMapping("ware/waresku")
public class WareSkuController {
    @Autowired
    private WareSkuService wareSkuService;


    /**
     * 供远程feign调用查看是否指定额skuId 有库存
     */
    @PostMapping("/checkStock/")
    public R checkStock(@RequestBody List<Long> skuIds) {
        List<SkuHasStockVo> skuHasStockVos = wareSkuService.checkSkusHasStock(skuIds);
        return R.ok().put("data",skuHasStockVos);
    }

    //feign 锁库存
    @PostMapping("/order/lock")
    public R lockOrderStock(@RequestBody WareSkuLockVo vo){
        Boolean lockOrderStock = null;
        try {
            lockOrderStock = wareSkuService.lockOrderStock(vo);
            return R.ok().put("locked",lockOrderStock);
        } catch (NoStockException e) {
            return R.error().put(BizCodeEnume.NO_STOCK_EXCEPTION.getCode().toString(),BizCodeEnume.NO_STOCK_EXCEPTION.getMsg());
        }
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = wareSkuService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody WareSkuEntity wareSku) {
        wareSkuService.save(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody WareSkuEntity wareSku) {
        wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
