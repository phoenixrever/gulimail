package com.phoenixhell.gulimall.seckill.controller;

import com.phoenixhell.common.to.mq.SecKillTo;
import com.phoenixhell.common.utils.R;
import com.phoenixhell.gulimall.seckill.To.SecKillSkuRedisTo;
import com.phoenixhell.gulimall.seckill.service.SecKillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller()
public class SecKillController {

    @Autowired
    SecKillService secKillService;

    //返回当前时间的秒杀商品
    @ResponseBody
    @GetMapping("/currentSecKillSkus")
    public R getCurrentSecKillSkus() {
        List<SecKillSkuRedisTo> secKillSkuRedisTos = secKillService.getCurrentSecKillSkus();
        return R.ok().put("data", secKillSkuRedisTos);
    }

    //获取当前sku的秒杀信息
    @ResponseBody
    @GetMapping("/info/{skuId}")
    public R getSecKillInfoBySkuId(@PathVariable Long skuId) {
        SecKillSkuRedisTo secKillSkuRedisTo = secKillService.getSecKillInfoBySkuId(skuId);
        return R.ok().put("data", secKillSkuRedisTo);
    }

    //秒杀
    @GetMapping("/kill")
    public String kill(@RequestParam("num") Integer num, @RequestParam("sessionId") String sessionId, @RequestParam("code") String code, Model model) {
        SecKillTo kill = secKillService.kill(sessionId, num, code);
        model.addAttribute("kill",kill);
        return "success";
    }
}
