package com.phoenixhell.gulimall.order.to;

import com.phoenixhell.gulimall.order.entity.OrderEntity;
import com.phoenixhell.gulimall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreatedOrderTo {
    //创建了哪个订单
    private OrderEntity order;

    //订单包含的订单项目
    private List<OrderItemEntity> orderItems;

    //订单计算后的价格
    private BigDecimal payPrice;

    //影响价格的因素 这里只算运费
    private BigDecimal fare;
}
