package com.phoenixhell.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.phoenixhell.common.to.mq.SecKillTo;
import com.phoenixhell.common.utils.PageUtils;
import com.phoenixhell.gulimall.order.entity.OrderEntity;
import com.phoenixhell.gulimall.order.vo.OrderConfirmVo;
import com.phoenixhell.gulimall.order.vo.OrderSubmitVo;
import com.phoenixhell.gulimall.order.vo.SubmitResponseVo;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author phoenixhell
 * @email phoenixrever@gmail.com
 * @date 2021-05-18 22:42:06
 */
public interface OrderService extends IService<OrderEntity> {

     void createSecKillOrder(SecKillTo secKillTo) throws ExecutionException, InterruptedException;

    PageUtils queryPage(Map<String, Object> params);

    OrderConfirmVo confirmOrder();

    SubmitResponseVo submit(OrderSubmitVo orderSubmitVo) throws Exception;

    void closeOrder(OrderEntity entity);

    PageUtils queryListOrderItems(Map<String, Object> params);
}

