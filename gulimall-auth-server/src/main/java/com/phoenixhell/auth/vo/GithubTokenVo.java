package com.phoenixhell.auth.vo;

import lombok.Data;

@Data
public class GithubTokenVo {
    private String accessToken;
    private String tokenType;
}
