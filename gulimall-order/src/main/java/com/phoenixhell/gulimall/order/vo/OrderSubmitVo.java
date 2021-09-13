package com.phoenixhell.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

// 封装订单提交数据
@Data
public class OrderSubmitVo {
    private  Long addressId;  //收获地址ID
    private Integer payType;  //支付方式
    //无需递交购物车商品，因为是实时从购物车获取的
    //优惠 发票 等等

    private String orderToken; //防重令牌

    private BigDecimal payment; //用户提交的应付价格

    //用户相关信息都在session

    private String note;  //订单备注
}
