package com.phoenixhell.gulimall.seckill.controller;

import com.phoenixhell.common.utils.R;
import com.phoenixhell.gulimall.seckill.To.SecKillSkuRedisTo;
import com.phoenixhell.gulimall.seckill.service.SecKillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController()
@RequestMapping("/seckill")
public class SecKillController {

    @Autowired
    SecKillService secKillService;

    //返回当前时间的秒杀商品
    @GetMapping("/currentSecKillSkus")
    public R getCurrentSecKillSkus(){
        List<SecKillSkuRedisTo> secKillSkuRedisTos=  secKillService.getCurrentSecKillSkus();
        return R.ok().put("data",secKillSkuRedisTos);
    }

    //获取当前sku的秒杀信息
    @GetMapping("/info/{skuId}")
    public R getSecKillInfoBySkuId(@PathVariable Long skuId){
        SecKillSkuRedisTo secKillSkuRedisTo =  secKillService.getSecKillInfoBySkuId(skuId);
        return R.ok().put("data",secKillSkuRedisTo);
    }
}
