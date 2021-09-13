package com.phoenixhell.gulimall.order.vo;

import com.phoenixhell.gulimall.order.entity.OrderEntity;
import com.phoenixhell.gulimall.order.entity.OrderItemEntity;
import lombok.Data;

import java.util.List;

@Data
public class PageVo {
    private OrderEntity orderEntity;
    private List<OrderItemEntity> orderItemEntities;
}
