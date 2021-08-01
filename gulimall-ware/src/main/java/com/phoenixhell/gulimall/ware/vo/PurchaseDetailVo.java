package com.phoenixhell.gulimall.ware.vo;

import lombok.Data;

@Data
public class PurchaseDetailVo {
    private Long itemId;//采购项id
    private Integer status;
    private String reason;
}
