package com.phoenixhell.gulimall.member.vo;

import lombok.Data;

@Data
public class MemberRegistVo {
    public String username;

    public String password;

    public String phone;

    private String githubId;

    private String header;
}
