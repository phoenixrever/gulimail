package com.phoenixhell.gulimall.product.vo;

import com.phoenixhell.common.to.SecKillSkuRedisTo;
import com.phoenixhell.gulimall.product.entity.SkuImagesEntity;
import com.phoenixhell.gulimall.product.entity.SkuInfoEntity;
import com.phoenixhell.gulimall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

@Data
public class SkuItemVo {
    //1 sku 基本信息获取  pms_sku_info
    SkuInfoEntity skuInfoEntity;

    Boolean hasStock=true;
    //2 sku的图片信息
    List<SkuImagesEntity> skuImagesEntities;
    //3 spu的销售属性组合(多少种sku组合)
    List<SkuItemSaleAttrVo> saleAttrVos;
    //4 获取spu的介绍
    SpuInfoDescEntity spuInfoDescEntity;
    //5 获取spu的规格参数信息
    List<SpuItemAttrGroupVo> groupAttrs;
    //秒杀信息
    SecKillSkuRedisTo secKillSkuRedisTo;

    @Data
    public static class SkuItemSaleAttrVo{
        private Long attrId;
        private String attrName;
        // 白色  对应的skuids  22,24,35
        private List<AttrValueWithSkuId> attrValueWithSkuIds;
    }

    @Data
    public static  class AttrValueWithSkuId {
        private String attrValue;
        private String skuIds;
    }


    @Data
    public static class SpuItemAttrGroupVo{
        private String groupName;
        private List<SpuBaseAttrVo> attrs;
    }

    @Data
    public static class SpuBaseAttrVo{
        private String attrName;
        private String attrValue;
    }
}
