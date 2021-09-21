package com.phoenixhell.common.to.mq;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SecKillTo {
    private String orderSn;
    /**
     * 活动场次id
     */
    private Long promotionSessionId;
    private Long skuId;
    private OrderItemEntityTo orderItemEntityTo;
    private BigDecimal seckillPrice;
    //购买数量
    private Integer num;
    //购买人
    private Long memberId;
}
