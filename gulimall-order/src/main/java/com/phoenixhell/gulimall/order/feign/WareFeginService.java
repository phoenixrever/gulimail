package com.phoenixhell.gulimall.order.feign;

import com.phoenixhell.common.utils.R;
import com.phoenixhell.gulimall.order.vo.WareSkuLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("gulimall-ware")
public interface WareFeginService {
    @PostMapping("/ware/waresku/checkStock/")
     R checkStock(@RequestBody List<Long> skuIds);

    @GetMapping("/ware/wareinfo/fare")
     R getFare(@RequestParam("addressId") Long addressId);

    @PostMapping("/ware/waresku/order/lock")
     R lockOrderStock(@RequestBody WareSkuLockVo vo);
}
