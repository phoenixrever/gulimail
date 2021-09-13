package com.phoenixhell.gulimall.cart.vo;

import lombok.Data;

@Data
public class UserInfoTo {
    private Long userId;
    private String userKey;

    //方便辨识是否有user-key 每个用户都必须有
    //为true 说明请求带来了user-key  不需要重新创建
    private Boolean tempKey=false;
}
