package com.phoenixhell.common.to.es;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * PUT product
 * {
 *     "mappings":{
 *         "properties": {
 *             "skuId":{ "type": "long" },
 *             "spuId":{ "type": "keyword" },  # 不可分词
 *             "skuTitle": {
 *                 "type": "text",
 *                 "analyzer": "ik_smart"  # 中文分词器
 *             },
 *             "skuPrice": { "type": "keyword" },  # 保证精度问题
 *     #只要冗余存储字段只是拿来查看的的都标上这2个属性，节省空间
 *             "skuImg"  : {
 *               "type": "keyword",
 *               "index": false, #不可以被当作关键字字段检索
 *               "doc_values": false  #不需要聚合，排序等操作
 *             },
 *             "saleCount":{ "type":"long" },
 *     #每次修改库存都会更新index，只有没货时候才更新index，设置布尔值更好
 *             "hasStock": { "type": "boolean" },
 *             "hotScore": { "type": "long"  },
 *             "brandId":  { "type": "long" },
 *             "catalogId": { "type": "long"  },
 *             "brandName": {
 *               "type": "keyword"
 *               "index": false,
 *               "doc_values": false
 *             },
 *             "brandImg":{
 *               "type": "keyword",
 *               "index": false,
 *               "doc_values": false
 *             },
 *             "catalogName": {
 *               "type": "keyword"
 *               "index": false,
 *               "doc_values": false
 *             },
 *             "attrs": {
 *               "type": "nested",
 *               "properties": {
 *                   "attrId": {"type": "long"  },
 *                   "attrName": {
 *                     "type": "keyword",
 *                     "index": false,
 *                     "doc_values": false
 *                   },
 *                 "attrValue": {"type": "keyword" }
 *               }
 *             }
 *         }
 *     }
 * }
 */
@Data
public class SkuEsModel {
    private Long skuId;
    private Long spuId;
    private String skuTitle;
    private BigDecimal skuPrice;
    private String skuImg;
    private Long saleCount;
    private boolean hasStock;
    private Long hotScore;
    private Long brandId;
    private Long catalogId;
    private String brandName;
    private String brandImg;
    private String catalogName;
    private List<Attr> attrs;


    @Data
    public static class Attr{
        private Long attrId;
        private String attrName;
        private String attrValue;
    }
}
