package com.phoenixhell.gulimall.ware.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderItemVo {
    private Long skuId;
    private String title;
    private String defaultImg;
    private List<String> skuAttr;
    private BigDecimal price;
    private Integer count;
    private Boolean hasStock;
    private BigDecimal totalPrice;
    private BigDecimal weight;
}
