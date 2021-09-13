package com.phoenixhell.gulimall.search.vo;

import com.phoenixhell.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.List;

@Data
public class SearchResult {
    //查询到的所有商品信息
    private List<SkuEsModel> products;

    private Integer pageNum;  //当前页码
    private Long total;    //总商品记录数
    private Long totalPages;    //总页数

    private List<BrandVo> brands; //当前查询到的结果所有涉及到的品牌

    private List<CatalogVo> catalogs;  //当前查询到的结果所有涉及到所有分类

    private List<AttrVo> attrs;  //当前查询到的结果所有涉及到的所有属性


    // 面包屑导航
    private List<NavVo> navs;

    @Data
    public static class NavVo{
        private String navName;
        private String navValue;
        private String link;
    }

    @Data
    public static class BrandVo {
        private Long brandId;
        private String brandName;
        private String brandImg;
    }

    @Data
    public static class CatalogVo {
        private Long catalogId;
        private String catalogName;
    }

    @Data
    public static class AttrVo {
        private Long attrId;
        private String attrName;
        private List<String> attrValues;
    }
}
