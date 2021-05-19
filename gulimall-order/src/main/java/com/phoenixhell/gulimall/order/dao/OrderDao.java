package com.phoenixhell.gulimall.order.dao;

import com.phoenixhell.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author phoenixhell
 * @email phoenixrever@gmail.com
 * @date 2021-05-18 22:42:06
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
