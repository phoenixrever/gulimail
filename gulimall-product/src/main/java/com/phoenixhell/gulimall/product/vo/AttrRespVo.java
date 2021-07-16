package com.phoenixhell.gulimall.product.vo;

import lombok.Data;

@Data
public class AttrRespVo extends AttrVo{

    /**
     * 分类
     */
    private String catalogName;

    /**
     * 分组
     */
    private String groupName;

    private Long[] catalogPath;
}
