package com.phoenixhell.gulimall.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.phoenixhell.common.utils.PageUtils;
import com.phoenixhell.common.utils.Query;
import com.phoenixhell.gulimall.coupon.dao.SeckillSessionDao;
import com.phoenixhell.gulimall.coupon.entity.SeckillSessionEntity;
import com.phoenixhell.gulimall.coupon.entity.SeckillSkuRelationEntity;
import com.phoenixhell.gulimall.coupon.service.SeckillSessionService;
import com.phoenixhell.gulimall.coupon.service.SeckillSkuRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Autowired
    SeckillSkuRelationService seckillSkuRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> getLast3DaysSessions() {
        LocalDateTime localDateTime = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime localDateTimeTo = LocalDateTime.now().withHour(23).withMinute(53).withSecond(59).plusDays(2);
        String time = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String timeTo = localDateTimeTo.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        List<SeckillSessionEntity> sessionEntities = this.query().between("start_time", time,timeTo).list();
        if(sessionEntities!=null && sessionEntities.size()>0){
            List<SeckillSessionEntity> sessions = sessionEntities.stream().map(seckillSessionEntity -> {
                List<SeckillSkuRelationEntity> skuRelationEntities = seckillSkuRelationService.query().eq("promotion_session_id", seckillSessionEntity.getId()).list();
                seckillSessionEntity.setSeckillSkuRelationEntities(skuRelationEntities);
                return seckillSessionEntity;
            }).collect(Collectors.toList());
            return sessions;
        }
        return null;
    }
}