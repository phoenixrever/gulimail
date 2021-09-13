package com.phoenixhell.gulimall.product.feign;

import com.phoenixhell.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("gulimall-ware")
public interface WareFeignService {
    /**
     * 1 R 设计的时候可以加上泛型
     * 2 直接返回我们想要的结果
     * 3 自己分装解析结果
     */
    @PostMapping("/ware/waresku/checkStock/")
    R checkStock(@RequestBody List<Long> skuIds);
}
