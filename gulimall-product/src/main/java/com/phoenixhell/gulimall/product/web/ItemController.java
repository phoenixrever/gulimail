package com.phoenixhell.gulimall.product.web;

import com.phoenixhell.gulimall.product.service.SkuInfoService;
import com.phoenixhell.gulimall.product.vo.SkuItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.concurrent.ExecutionException;

@Controller
public class ItemController {
    @Autowired
    private SkuInfoService skuInfoService;
    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable Long skuId, Model model) throws ExecutionException, InterruptedException {
        SkuItemVo skuItemVo = skuInfoService.itemDetail(skuId);
        model.addAttribute("item",skuItemVo);
        System.out.println(skuId);
        return "item";
    }
}
