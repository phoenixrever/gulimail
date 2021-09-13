package com.phoenixhell.auth.vo;

import lombok.Data;

@Data
public class UserLoginVo {
    //手机 用户名都可以登录
    private String loginAccount;
    private String password;
}
