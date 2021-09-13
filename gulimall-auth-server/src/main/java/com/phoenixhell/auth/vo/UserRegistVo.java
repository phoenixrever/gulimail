package com.phoenixhell.auth.vo;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
public class UserRegistVo {
    //是否存在在service里面去查
//    @NotEmpty(message = "用户名不能为空")  注意 NotEmpty 不能和pattern同时存在
    @Pattern(regexp ="^[0-9A-Za-z]{6,16}$",message = "必须是6-16位的数字或字母")
    public String username;

    // 包含数字字符
    //?![0-9]+$) 不全是数字
    //(?![a-zA-Z]+$) 不全是字母
    @Pattern(regexp = "^(?![0-9]+$)(?![a-zA-Z]+$)[a-zA-Z0-9\\S]{8,16}$",message = "必须是数字和字母的组合")
    public String password;

    @Pattern(regexp = "^1[3-9][0-9]{9}$",message ="手机号格式不正确" )
    public String phone;

    @NotEmpty(message = "验证码不能为空")
    public String code;
}
