package com.phoenixhell.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.phoenixhell.common.utils.PageUtils;
import com.phoenixhell.common.utils.Query;
import com.phoenixhell.common.utils.R;
import com.phoenixhell.gulimall.ware.dao.WareSkuDao;
import com.phoenixhell.gulimall.ware.entity.WareSkuEntity;
import com.phoenixhell.gulimall.ware.feign.ProductSkuNameService;
import com.phoenixhell.gulimall.ware.service.WareSkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {
    @Autowired
    WareSkuDao wareSkuDao;
    @Autowired
    ProductSkuNameService productSkuNameService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();
        String wareId = (String) params.get("wareId");
        String skuId = (String) params.get("skuId");
        wrapper.eq(!StringUtils.isEmpty(wareId),"ware_id", wareId);
        wrapper.eq(!StringUtils.isEmpty(skuId),"sku_id", skuId);

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),wrapper);

        return new PageUtils(page);
    }
    @Transactional
    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        //如果没有库存记录 新增
        List<WareSkuEntity> skuEntities = this.query().eq("sku_id", skuId).list();
        if(skuEntities.size()==0){
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setStockLocked(0);
            //远程查询sku_name 商品名称 失败整个事务不需要回滚
            //1catch掉异常
            //todo 其他方法异常不回滚
            try {
                R info = productSkuNameService.info(skuId);
                if(info.getCode()==0){
                    Map<String,Object> skuInfo = (Map<String, Object>) info.get("skuInfo");
                    wareSkuEntity.setSkuName((String) skuInfo.get("skuName"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.save(wareSkuEntity);
        }else{
            wareSkuDao.addStock(skuId,wareId,skuNumm);
        }
    }

}