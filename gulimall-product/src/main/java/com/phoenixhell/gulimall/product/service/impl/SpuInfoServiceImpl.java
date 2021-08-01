package com.phoenixhell.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.phoenixhell.common.to.SkuReductionTo;
import com.phoenixhell.common.to.SpuBoundsTo;
import com.phoenixhell.common.utils.PageUtils;
import com.phoenixhell.common.utils.Query;
import com.phoenixhell.common.utils.R;
import com.phoenixhell.gulimall.product.dao.SpuInfoDao;
import com.phoenixhell.gulimall.product.entity.*;
import com.phoenixhell.gulimall.product.feign.CouponFeignService;
import com.phoenixhell.gulimall.product.service.*;
import com.phoenixhell.gulimall.product.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {
    @Autowired
    private SpuInfoDescService spuInfoDescService;
    @Autowired
    private SpuImagesService spuImagesService;
    @Autowired
    private AttrService attrService;
    @Autowired
    private ProductAttrValueService productAttrValueService;
    @Autowired
    private SkuInfoService skuInfoService;
    @Autowired
    private SkuImagesService skuImagesService;
    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;
    @Autowired
    private CouponFeignService couponFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo spuVo) {
        //1)保存spu 基本信息  pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(spuVo, spuInfoEntity);
        this.save(spuInfoEntity);

        //2)保存spu的描述图片pms_spu_info_desc
        List<String> descript = spuVo.getDescript();
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
        spuInfoDescEntity.setDescript(String.join(",", descript));
        spuInfoDescService.save(spuInfoDescEntity);

        //3)保存spu的图片集 pms_spu_images
        List<String> images = spuVo.getImages();
        if (images != null && images.size() > 0) {
            List<SpuImagesEntity> spuImagesEntityList = images.stream().map(image -> {
                SpuImagesEntity spuImagesEntity = new SpuImagesEntity();
                spuImagesEntity.setSpuId(spuInfoEntity.getId());
                spuImagesEntity.setImgUrl(image);
                return spuImagesEntity;
            }).filter(entity -> !StringUtils.isEmpty(entity.getImgUrl())).collect(Collectors.toList());
            spuImagesService.saveBatch(spuImagesEntityList);
        }

        //4) 保存spu的规格参数pms_product_attr_value
        List<BaseAttrs> baseAttrs = spuVo.getBaseAttrs();
        if (baseAttrs != null && baseAttrs.size() > 0) {
            List<ProductAttrValueEntity> collect = baseAttrs.stream().map(baseAttr -> {
                ProductAttrValueEntity attrValueEntity = new ProductAttrValueEntity();
                attrValueEntity.setSpuId(spuInfoEntity.getId());
                attrValueEntity.setAttrId(baseAttr.getAttrId());
                AttrEntity attrEntity = attrService.getById(baseAttr.getAttrId());
                attrValueEntity.setAttrName(attrEntity.getAttrName());
                attrValueEntity.setAttrValue(baseAttr.getAttrValues());
                attrValueEntity.setQuickShow(baseAttr.getShowDesc());
                return attrValueEntity;
            }).collect(Collectors.toList());
            productAttrValueService.saveBatch(collect);
        }

        //保存spu的积分信息gulimall_sms 中的 sms_spu_bounds 表
        Bounds bounds = spuVo.getBounds();
        SpuBoundsTo spuBoundsTo = new SpuBoundsTo();
        BeanUtils.copyProperties(bounds, spuBoundsTo);
        spuBoundsTo.setSpuId(spuInfoEntity.getId());

        R r = couponFeignService.saveSpuBounds(spuBoundsTo);
        if (r.getCode() != 0) {
            System.out.println("远程保存spu积分信息失败");
            //todo 分布式事务
//            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST);
        }
        //==================保存当前spu对应的sku信息===================
        List<Skus> skus = spuVo.getSkus();
        if (skus != null && skus.size() > 0) {
            skus.forEach(sku -> {
                //    private String skuName;
                //    private BigDecimal price;
                //    private String skuTitle;
                //    private String skuSubtitle;
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(sku, skuInfoEntity);
                //添加其他没有的属性
                skuInfoEntity.setSpuId(spuInfoEntity.getId());
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);

                sku.getImages().forEach(image -> {
                    if (image.getDefaultImg() == 1) {
                        skuInfoEntity.setSkuDefaultImg(image.getImgUrl());
                    }
                });
                //1) sku的基本信息 pms_spu_info
                skuInfoService.save(skuInfoEntity);
                Long skuId = skuInfoEntity.getSkuId();

                List<Images> skuImages = sku.getImages();
                if (skuImages != null && skuImages.size() > 0) {
                    List<SkuImagesEntity> imagesEntities = sku.getImages().stream().map(image -> {
                        //保存图片先要保存sku 得到skuId不能在这里面保存默认图片
                        SkuImagesEntity imagesEntity = new SkuImagesEntity();
                        imagesEntity.setSkuId(skuId);
                        imagesEntity.setImgUrl(image.getImgUrl());
                        imagesEntity.setDefaultImg(image.getDefaultImg());
                        return imagesEntity;
                    }).filter(entity -> !StringUtils.isEmpty(entity.getImgUrl())).collect(Collectors.toList());
                    //2)sku 的所有图片信息pms_sku_images
                    skuImagesService.saveBatch(imagesEntities);
                }

                //3)sku的销售属性信息pms_sku_sale_attr_value
                List<Attr> attr = sku.getAttr();
                List<SkuSaleAttrValueEntity> saleAttrValueEntities = attr.stream().map(item -> {
                    SkuSaleAttrValueEntity saleAttrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(item, saleAttrValueEntity);
                    saleAttrValueEntity.setSkuId(skuId);
                    return saleAttrValueEntity;
                }).collect(Collectors.toList());
                skuSaleAttrValueService.saveBatch(saleAttrValueEntities);

                //sku的优惠， 满减信息 gulimall_sms->sms_sku_ladder\sms_sku_full_reduction\sms_member_price
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(sku, skuReductionTo);
                skuReductionTo.setSkuId(skuId);

                //bigdecimal compareTo
                if (skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal(0)) == 1) {
                    R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
                    if (r1.getCode() != 0) {
                        System.out.println("远程保存sku的优惠满减信息信息失败");
                        //todo 分布式事务
//                    throw new HttpClientErrorException(HttpStatus.BAD_REQUEST);
                    }
                }

            });


        }
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        String status = (String) params.get("status");
        String brandId = (String) params.get("brandId");
        String catalogId = (String) params.get("catalogId");
        if(!StringUtils.isEmpty(key)){
            wrapper.and(item->{
                item.eq("id", key).or().like("spu_name", key);
            });
        }

        if(!StringUtils.isEmpty(status)){
            wrapper.eq("publish_status",status);
        }

        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            wrapper.eq("brand_id", brandId);
        }

        if (!StringUtils.isEmpty(catalogId)&&!"0".equalsIgnoreCase(catalogId)) {
            wrapper.eq("catalog_id", catalogId);
        }

        IPage<SpuInfoEntity> page = this.page(new Query<SpuInfoEntity>().getPage(params), wrapper);
        return new PageUtils(page);
    }
}