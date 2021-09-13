package com.phoenixhell.gulimall.member.vo;

import lombok.Data;

@Data
public class MemberLoginVo {
    //手机 用户名都可以登录
    private String loginAccount;
    private String password;
}

