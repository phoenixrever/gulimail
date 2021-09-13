package com.phoenixhell.auth.feign;

import com.phoenixhell.auth.vo.GithubRegistVo;
import com.phoenixhell.auth.vo.UserLoginVo;
import com.phoenixhell.auth.vo.UserRegistVo;
import com.phoenixhell.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("gulimall-member")
public interface MemberFeignService {
    @PostMapping("/member/member/regist")
    R regist(@RequestBody UserRegistVo vo);

    //远程调用用的是json
    @PostMapping("/member/member/login")
     R login(@RequestBody UserLoginVo vo);

    @PostMapping("/member/member/oauth2Login")
     R oauth2Login(@RequestBody GithubRegistVo vo);
}
