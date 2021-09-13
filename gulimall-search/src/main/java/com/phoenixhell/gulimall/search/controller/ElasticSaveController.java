package com.phoenixhell.gulimall.search.controller;

import com.phoenixhell.common.exception.BizCodeEnume;
import com.phoenixhell.common.to.es.SkuEsModel;
import com.phoenixhell.common.utils.R;
import com.phoenixhell.gulimall.search.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequestMapping("/search/save")
@RestController
public class ElasticSaveController {
    @Autowired
    private ProductService productService;

    //上架商品
    @PostMapping("/product")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels) {
        Boolean up = false;
        try {
            up = productService.productStatusUp(skuEsModels);

        } catch (IOException e) {
            log.error("商品上架异常", e);
            return R.error(BizCodeEnume.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnume.PRODUCT_UP_EXCEPTION.getMsg());
        }
        if (!up) return R.error(BizCodeEnume.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnume.PRODUCT_UP_EXCEPTION.getMsg());
        return R.ok();
    }
}
