package com.phoenixhell.auth.vo;

import lombok.Data;

@Data
public class GithubRegistVo {
    public String username;

    private String githubId;

    private String header;
}
