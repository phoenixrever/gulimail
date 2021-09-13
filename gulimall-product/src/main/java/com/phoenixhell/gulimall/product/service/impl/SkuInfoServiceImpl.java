package com.phoenixhell.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.phoenixhell.common.to.SecKillSkuRedisTo;
import com.phoenixhell.common.utils.PageUtils;
import com.phoenixhell.common.utils.Query;
import com.phoenixhell.common.utils.R;
import com.phoenixhell.gulimall.product.dao.SkuInfoDao;
import com.phoenixhell.gulimall.product.entity.SkuImagesEntity;
import com.phoenixhell.gulimall.product.entity.SkuInfoEntity;
import com.phoenixhell.gulimall.product.entity.SpuInfoDescEntity;
import com.phoenixhell.gulimall.product.feign.SeckillFeignService;
import com.phoenixhell.gulimall.product.service.*;
import com.phoenixhell.gulimall.product.vo.SkuItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    private SkuImagesService skuImagesService;
    @Autowired
    private SpuInfoDescService spuInfoDescService;
    @Autowired
    private AttrGroupService attrGroupService;
    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;
    @Autowired
    private ThreadPoolExecutor executor;
    @Autowired
    private SeckillFeignService seckillFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> wrapper = new QueryWrapper<>();
        //key:
        //catalogId:
        //brandId:
        //min: 0
        //max: 0
        String key = (String) params.get("key");
        String minPrice = (String) params.get("min");
        String maxPrice = (String) params.get("max");
        String brandId = (String) params.get("brandId");
        String catalogId = (String) params.get("catalogId");

        if (!StringUtils.isEmpty(key)) {
            wrapper.and(item -> {
                item.eq("sku_id", key).or().like("sku_name", key);
            });
        }


        wrapper.ge(!StringUtils.isEmpty(minPrice), "price", minPrice);


        if (!StringUtils.isEmpty(maxPrice)) {
            try {
                BigDecimal bigDecimal = new BigDecimal(maxPrice);
                if (bigDecimal.compareTo(new BigDecimal(0)) == 1) {
                    wrapper.le("price", maxPrice);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }


        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            wrapper.eq("brand_id", brandId);
        }

        if (!StringUtils.isEmpty(catalogId) && !"0".equalsIgnoreCase(catalogId)) {
            wrapper.eq("catalog_id", catalogId);
        }
        IPage<SkuInfoEntity> page = this.page(new Query<SkuInfoEntity>().getPage(params), wrapper);

        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        List<SkuInfoEntity> skuInfoEntities = this.query().eq("spu_id", spuId).list();
        return skuInfoEntities;
    }

    @Override
    public SkuItemVo itemDetail(Long skuId) throws ExecutionException, InterruptedException {
        SkuItemVo skuItemVo = new SkuItemVo();
        CompletableFuture<SkuInfoEntity> future = CompletableFuture.supplyAsync(() -> {
            //1 sku 基本信息获取  pms_sku_info
            SkuInfoEntity skuInfoEntity = this.getById(skuId);
            skuItemVo.setSkuInfoEntity(skuInfoEntity);
            //返回 其他线程要用到的结果
            return skuInfoEntity;
        }, executor);

        //3线程异步获取信息
        CompletableFuture<Void> skuItemSaleAttrFuture = future.thenAcceptAsync((res) -> {
            //3 spu的销售属性组合(多少种sku组合)
            List<SkuItemVo.SkuItemSaleAttrVo> skuItemSaleAttrVos = skuSaleAttrValueService.getSaleAttrsBySpuId(res.getSpuId());
            skuItemVo.setSaleAttrVos(skuItemSaleAttrVos);
        }, executor);

        CompletableFuture<Void> spuInfoDescFuture = future.thenAcceptAsync((res) -> {
            //4 获取spu的介绍
            SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(res.getSpuId());
            skuItemVo.setSpuInfoDescEntity(spuInfoDescEntity);
        }, executor);

        CompletableFuture<Void> spuItemAttrGroupFuture = future.thenAcceptAsync(res -> {
            //5 获取spu的规格参数信息
            final List<SkuItemVo.SpuItemAttrGroupVo> spuItemAttrGroupVos = attrGroupService.getAttrGroupWithAttrBySpuId(res.getSpuId(), res.getCatalogId());
            skuItemVo.setGroupAttrs(spuItemAttrGroupVos);
        }, executor);


        //2 主线程再开线程异步获取sku的图片信息
        CompletableFuture<Void> SkuImageFuture = CompletableFuture.runAsync(() -> {
            List<SkuImagesEntity> imagesEntities = skuImagesService.query().eq("sku_id", skuId).list();
            skuItemVo.setSkuImagesEntities(imagesEntities);
        }, executor);

        //远程服务获取上去skuId商品秒杀信息
        CompletableFuture<Void> secKillFuture = CompletableFuture.runAsync(() -> {
            R r = seckillFeignService.getSecKillInfoBySkuId(skuId);
            if (r.getCode() == 0) {
                SecKillSkuRedisTo data = r.getData(new TypeReference<SecKillSkuRedisTo>() {
                });
                skuItemVo.setSecKillSkuRedisTo(data);
            }
        });
        //等待所有任务都完成
        CompletableFuture<Void> allOf = CompletableFuture.allOf(
                skuItemSaleAttrFuture,
                spuInfoDescFuture,
                spuItemAttrGroupFuture,
                SkuImageFuture,
                secKillFuture
        );

        allOf.get();
        return skuItemVo;
    }
}