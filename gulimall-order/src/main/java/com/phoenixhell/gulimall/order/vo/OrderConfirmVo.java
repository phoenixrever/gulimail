package com.phoenixhell.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;


//订单确认页面需要用的数据
@Data
public class OrderConfirmVo {
    //收获地址ums_member_receive_address  每个人可以哟i好几个收货地址
    private List<MemberAddressVo> addressList;

    //所有选中的购物项
    private List<OrderItemVo> items;

    //优惠券 积分	integration 结合; 整合; 一体化
    private Integer integration;

    //订单总额
    private BigDecimal total;

    //商品总个数
    public Integer getTotalCount(){
        Integer total = items.stream().map(item -> item.getCount()).reduce(0, (preValue, currentValue) -> preValue + currentValue);
        return total;
    }

    //实际付款
    private BigDecimal payment;

    // 订单唯一令牌防止无限次提交
    private String orderToken;
}
