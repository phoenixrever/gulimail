package com.phoenixhell.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

@Data
public class WareSkuLockVo {
    //订单号
    private String orderSn;

    //所有需要锁住的库存信息
    private List<OrderItemVo> locks;
}
