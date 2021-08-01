package com.phoenixhell.gulimall.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.phoenixhell.common.to.MemberPrice;
import com.phoenixhell.common.to.SkuReductionTo;
import com.phoenixhell.common.utils.PageUtils;
import com.phoenixhell.common.utils.Query;
import com.phoenixhell.gulimall.coupon.dao.SkuFullReductionDao;
import com.phoenixhell.gulimall.coupon.entity.MemberPriceEntity;
import com.phoenixhell.gulimall.coupon.entity.SkuFullReductionEntity;
import com.phoenixhell.gulimall.coupon.entity.SkuLadderEntity;
import com.phoenixhell.gulimall.coupon.service.MemberPriceService;
import com.phoenixhell.gulimall.coupon.service.SkuFullReductionService;
import com.phoenixhell.gulimall.coupon.service.SkuLadderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {
    @Autowired
    private SkuLadderService skuLadderService;
    @Autowired
    private SkuFullReductionService skuFullReductionService;
    @Autowired
    private MemberPriceService memberPriceService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuReduction(SkuReductionTo skuReductionTo) {
        //sku的优惠， 满减信息 gulimall_sms->sms_sku_ladder\sms_sku_full_reduction\sms_member_price

        //sms_sku_ladder
        if(skuReductionTo.getFullCount()>0){
            SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
            skuLadderEntity.setSkuId(skuReductionTo.getSkuId());
            skuLadderEntity.setFullCount(skuReductionTo.getFullCount());
            skuLadderEntity.setDiscount(skuReductionTo.getDiscount());
            //是否叠加其他优惠[0-不可叠加，1-可叠加]
            skuLadderEntity.setAddOther(skuReductionTo.getCountStatus());
            skuLadderService.save(skuLadderEntity);
        }


        //sms_sku_full_reduction
        if(skuReductionTo.getFullPrice().compareTo(new BigDecimal(0))==1){
            SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
            BeanUtils.copyProperties(skuReductionTo, skuFullReductionEntity);
            skuFullReductionEntity.setAddOther(skuReductionTo.getCountStatus());
            this.save(skuFullReductionEntity);
        }


        //sms_member_price
        List<MemberPrice> memberPrice = skuReductionTo.getMemberPrice();
        List<MemberPriceEntity> memberPriceEntities = memberPrice.stream().map(item -> {
            MemberPriceEntity memberPriceEntity = new MemberPriceEntity();
            memberPriceEntity.setSkuId(skuReductionTo.getSkuId());
            memberPriceEntity.setMemberLevelId(item.getId());
            memberPriceEntity.setMemberLevelName(item.getName());
            memberPriceEntity.setMemberPrice(item.getPrice());
            memberPriceEntity.setAddOther(1);
            return memberPriceEntity;
        }).filter(entity->entity.getMemberPrice().compareTo(new BigDecimal(0))==1).collect(Collectors.toList());
        memberPriceService.saveBatch(memberPriceEntities);
    }

}