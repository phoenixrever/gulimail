package com.phoenixhell.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.phoenixhell.common.utils.PageUtils;
import com.phoenixhell.common.utils.Query;
import com.phoenixhell.common.utils.R;
import com.phoenixhell.common.vo.MemberAddressVo;
import com.phoenixhell.gulimall.ware.dao.WareInfoDao;
import com.phoenixhell.gulimall.ware.entity.WareInfoEntity;
import com.phoenixhell.gulimall.ware.feign.MemberFeignService;
import com.phoenixhell.gulimall.ware.service.WareInfoService;
import com.phoenixhell.gulimall.ware.vo.FareVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Map;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {
    @Autowired
    private MemberFeignService memberFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                new QueryWrapper<WareInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<WareInfoEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and(w -> {
                w.eq("id", key).or().like("name", key)
                        .or().like("areacode", key)
                        .or().like("address", key);
            });
        }
        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params), wrapper);
        return new PageUtils(page);
    }

    //根据收获地址计算运费
    @Override
    public FareVo getFare(Long addressId) {
        //远程获取member 微服务的收货地址
        R info = memberFeignService.info(addressId);
        if(info.getCode()!=0){
            return null;
        }
        MemberAddressVo receiveAddress =info.getData("memberReceiveAddress", new TypeReference<MemberAddressVo>() {});
        //手机号最后一位模拟运费
        String phone = receiveAddress.getPhone();
        String fareString = receiveAddress.getPhone().substring(phone.length()-1,phone.length());
        BigDecimal fare = new BigDecimal(fareString);
        FareVo fareVo = new FareVo();
        fareVo.setFare(fare);
        fareVo.setMemberAddressVo(receiveAddress);
        return fareVo;
    }

}