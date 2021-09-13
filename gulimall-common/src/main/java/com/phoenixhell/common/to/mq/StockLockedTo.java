package com.phoenixhell.common.to.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class StockLockedTo {
    //库存工作单id
    private long id;

    // 工作单详情ids 一个工作单(订单) 有多个工作单详情(多个购物项)
    private List<WareOrderTaskDetailTo>  details;

}
