package com.phoenixhell.gulimall.order.feign;

import com.phoenixhell.gulimall.order.vo.MemberAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient("gulimall-member")
public interface MemberFeignService {
    // 远程获取会员收获地址feign接口
    @GetMapping("/member/memberreceiveaddress/{memberId}/addresses")
     List<MemberAddressVo> getAddress(@PathVariable Long memberId);
}
