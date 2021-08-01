package com.phoenixhell.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.phoenixhell.common.constant.WareConstant;
import com.phoenixhell.common.utils.PageUtils;
import com.phoenixhell.common.utils.Query;
import com.phoenixhell.gulimall.ware.dao.PurchaseDao;
import com.phoenixhell.gulimall.ware.entity.PurchaseDetailEntity;
import com.phoenixhell.gulimall.ware.entity.PurchaseEntity;
import com.phoenixhell.gulimall.ware.service.PurchaseDetailService;
import com.phoenixhell.gulimall.ware.service.PurchaseService;
import com.phoenixhell.gulimall.ware.service.WareSkuService;
import com.phoenixhell.gulimall.ware.vo.MergeVo;
import com.phoenixhell.gulimall.ware.vo.PurchaseDetailVo;
import com.phoenixhell.gulimall.ware.vo.PurchaseDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {
    @Autowired
    private PurchaseDetailService purchaseDetailService;
    @Autowired
    private WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceive(Map<String, Object> params) {
        QueryWrapper<PurchaseEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 0).or().eq("status", 1);

        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params), wrapper);

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void mergePurchase(MergeVo mergeVo) {
        int create=WareConstant.PurchaseStatusEnum.CREATED.getCode();
        int assigned=WareConstant.PurchaseStatusEnum.ASSIGNED.getCode();

        Long purchaseId = mergeVo.getPurchaseId();
        if (purchaseId == null) {
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }

        PurchaseEntity purchaseEntityToMerge = this.getById(purchaseId);
        if (!(purchaseEntityToMerge.getStatus() == create || purchaseEntityToMerge.getStatus() == assigned)) {
            throw  new RuntimeException("要合并的采购单已经被领取");
        }
        mergeVo.getItems().forEach(id->{

            boolean b = purchaseDetailService.getById(id).getStatus() == create || purchaseDetailService.getById(id).getStatus() == assigned;
            if(!b){
                throw  new RuntimeException("要合并的采购需项目正在采购");
            }
        });

        List<Long> ids = mergeVo.getItems();
        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> collect = ids.stream().map(id -> {
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            detailEntity.setId(id);
            detailEntity.setPurchaseId(finalPurchaseId);
            detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.assigned.getCode());
            return detailEntity;
        }).collect(Collectors.toList());
        purchaseDetailService.updateBatchById(collect);
    }

    @Override
    public void received(List<Long> ids) {
        //1) 新建当前采购单是新建或者已经分配状态
        List<PurchaseEntity> purchaseEntities = this.query().in("id", ids).and(q ->
                q.eq("status", WareConstant.PurchaseStatusEnum.CREATED.getCode())
                        .or().eq("status", WareConstant.PurchaseStatusEnum.ASSIGNED.getCode())).list();

        /*2种方法都能查
        QueryWrapper<PurchaseEntity> wrapper = new QueryWrapper<>();
        wrapper.in("id",ids);
        wrapper.and(w->{
            w.eq("status",WareConstant.PurchaseStatusEnum.CREATED.getCode())
                    .or().eq("status",WareConstant.PurchaseStatusEnum.CREATED.getCode());
        });
        List<PurchaseEntity> purchaseEntities = this.list(wrapper);*/
        List<PurchaseEntity> collect = purchaseEntities.stream().map(p -> {
            p.setStatus(WareConstant.PurchaseStatusEnum.RECEIVE.getCode());
            return p;
        }).collect(Collectors.toList());
        this.updateBatchById(collect);
        //2) 改变采购单的状态
        purchaseEntities.forEach(p->{
            List<PurchaseDetailEntity> purchaseDetailEntities=purchaseDetailService.listDetailByPurchaseId(p.getId());
            List<PurchaseDetailEntity> newPurchaseDetailEntities = purchaseDetailEntities.stream().map(item -> {
                //只需要更新status 其它值不传 和直接用查出来的有什么区别暂时还不知道
                PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
                purchaseDetailEntity.setId(item.getId());
                purchaseDetailEntity.setStatus(WareConstant.PurchaseStatusEnum.RECEIVE.getCode());
                return purchaseDetailEntity;
            }).collect(Collectors.toList());
            purchaseDetailService.updateBatchById(newPurchaseDetailEntities);
        });
        //3） 改变采购项的状态
    }

    @Transactional
    @Override
    public void done(PurchaseDoneVo doneVo) {
        //2) 改变每个采购单每个采购项目的状态
        boolean flag=true;
        List<PurchaseDetailVo> items = doneVo.getItems();
        ArrayList<PurchaseDetailEntity> purchaseDetailEntities = new ArrayList<>();
        for(PurchaseDetailVo item:items){
            PurchaseDetailEntity entity = new PurchaseDetailEntity();
            entity.setStatus(item.getStatus());
            entity.setId(item.getItemId());
            purchaseDetailEntities.add(entity);
            if(item.getStatus()==WareConstant.PurchaseStatusEnum.HASERROR.getCode()){
                flag=false;
            }else{
                //3) 将成功的采购进行入库
                PurchaseDetailEntity detailEntity = purchaseDetailService.getById(item.getItemId());
                wareSkuService.addStock(detailEntity.getSkuId(),detailEntity.getWareId(),detailEntity.getSkuNum());
            }
        }
        purchaseDetailService.updateBatchById(purchaseDetailEntities);
        //1) 改变采购单状态
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        Long purchaseId = doneVo.getId();
        purchaseEntity.setId(purchaseId);
        if(flag){
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.FINISH.getCode());
        }else{
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.HASERROR.getCode());
        }
        this.updateById(purchaseEntity);
    }

}