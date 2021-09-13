package com.phoenixhell.gulimall.search.vo;

import lombok.Data;

import java.util.List;

/**
 *  封装页面所有可能传过来的检索条件
 */
@Data
public class SearchParam {
    private String keyword;  //页面传过来的全文匹配关键字
    private Long catalog3Id; //三级分类id

    /**
     *  //排序
     *  sort=saleCount-asc/desc
     *  sort=hotScore-asc/desc   综合排序（热度评分）
     *  sort=skuPrice-asc/desc
     */

    private String sort;
    /**
     * 是否只显示有货
     * hasStock= 0 / 1
     */
    private Integer hasStock;

    /**
     * 价格区间
     * 100-1000  / -1000  /100-
     */
    private String skuPrice;

    /**
     * 品牌ID 多选
     */
    private List<Long> brandIds;

    /**
     * 按照属性筛选
     * attr=系统(属性ID)-windows:ios:android:linux
     * attr=尺寸(属性ID)-12.1:13.5:14.2:15.6
     */
    private List<String> attrs;

    private String url;
    private Integer pageNum=1;
}
