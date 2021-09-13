package com.phoenixhell.gulimall.ware.vo;

import lombok.Data;

@Data
public class LockStockResultVo {
    private Long skuId;
    private Integer lockNum;
    private boolean locked;
}
