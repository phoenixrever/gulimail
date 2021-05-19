package com.phoenixhell.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.phoenixhell.common.utils.PageUtils;
import com.phoenixhell.gulimall.member.entity.IntegrationChangeHistoryEntity;

import java.util.Map;

/**
 * 积分变化历史记录
 *
 * @author phoenixhell
 * @email phoenixrever@gmail.com
 * @date 2021-05-18 22:34:06
 */
public interface IntegrationChangeHistoryService extends IService<IntegrationChangeHistoryEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

