package com.phoenixhell.gulimall.order.vo;

import com.phoenixhell.gulimall.order.entity.OrderEntity;
import lombok.Data;

@Data
public class SubmitResponseVo {
    private OrderEntity order;
    private Integer code=0;  //错误状态吗 0 成功
}
