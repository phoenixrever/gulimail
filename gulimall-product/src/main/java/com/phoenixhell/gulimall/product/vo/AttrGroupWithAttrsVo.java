package com.phoenixhell.gulimall.product.vo;

import com.phoenixhell.gulimall.product.entity.AttrEntity;
import com.phoenixhell.gulimall.product.entity.AttrGroupEntity;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AttrGroupWithAttrsVo  extends AttrGroupEntity {
    //前端固定这个名字attrs
    private List<AttrEntity> attrs=new ArrayList<>();
}
