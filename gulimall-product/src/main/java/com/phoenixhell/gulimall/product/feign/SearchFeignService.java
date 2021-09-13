package com.phoenixhell.gulimall.product.feign;

import com.phoenixhell.common.to.es.SkuEsModel;
import com.phoenixhell.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("gulimall-search")
public interface SearchFeignService {
    //上架商品
    @PostMapping("/search/save/product")
     R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels);
}
