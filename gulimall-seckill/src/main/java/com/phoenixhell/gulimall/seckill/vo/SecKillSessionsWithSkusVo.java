package com.phoenixhell.gulimall.seckill.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class SecKillSessionsWithSkusVo {
    /**
     * id
     */
    private Long id;
    /**
     * 场次名称
     */
    private String name;
    /**
     * 每日开始时间
     */
    private Date startTime;
    /**
     * 每日结束时间
     */
    private Date endTime;
    /**
     * 启用状态
     */
    private Integer status;

    /**
     * 秒杀时间段关联的秒杀商品 注意不是数据库字段 @TableField(exist = false)
     */
    private List<SeckillSkuRelationEntity> seckillSkuRelationEntities;

    /**
     * 创建时间
     */
    private Date createTime;

    @Data
    public static  class  SeckillSkuRelationEntity{
        /**
         * id
         */
        private Long id;
        /**
         * 活动id
         */
        private Long promotionId;
        /**
         * 活动场次id
         */
        private Long promotionSessionId;
        /**
         * 商品id
         */
        private Long skuId;
        /**
         * 秒杀价格
         */
        private BigDecimal seckillPrice;
        /**
         * 秒杀总量
         */
        private Integer seckillCount;
        /**
         * 每人限购数量
         */
        private Integer seckillLimit;
        /**
         * 排序
         */
        private Integer seckillSort;

    }

}
