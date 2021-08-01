package com.phoenixhell.common.to;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
@Data
public class SkuReductionTo {
    private Long skuId;
    //不能是integer new出来初始值是null
    private int fullCount;
    private BigDecimal discount;
    //是否叠加其他优惠
    private int countStatus;
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private int priceStatus;
    private List<MemberPrice> memberPrice;
}
