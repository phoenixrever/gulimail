package com.phoenixhell.gulimall.ware.vo;

import com.phoenixhell.common.vo.MemberAddressVo;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FareVo {
    private MemberAddressVo memberAddressVo;
    private BigDecimal fare;
}
