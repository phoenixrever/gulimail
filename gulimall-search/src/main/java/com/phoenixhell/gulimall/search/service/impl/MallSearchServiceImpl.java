package com.phoenixhell.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.phoenixhell.common.to.es.SkuEsModel;
import com.phoenixhell.gulimall.search.config.ElasticSearchConfig;
import com.phoenixhell.gulimall.search.constant.EsConstant;
import com.phoenixhell.gulimall.search.service.MallSearchService;
import com.phoenixhell.gulimall.search.vo.SearchParam;
import com.phoenixhell.gulimall.search.vo.SearchResult;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service("mallSearchService")
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * 返回 search 页面包含的所有信息
     *
     * @param param
     * @return
     */
    public SearchResult search(SearchParam param) throws IOException {
        // 1 创建检索请求 参数为要检索的索引
        // SearchRequest searchRequest = new SearchRequest(EsConstant.PRODUCT_INDEX);

        //QueryBuilders 构造检索
        //SearchSourceBuilder组合检索
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        // 构建bool query
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // must 配对
        if (!StringUtils.isEmpty(param.getKeyword())) {
            //todo 模糊搜索 过滤等 等学了elasticsearch 在添加
//            boolQueryBuilder.must().add(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
            boolQueryBuilder.must().add(QueryBuilders.fuzzyQuery("skuTitle", param.getKeyword()));;
        }
        //filter

        if (param.getCatalog3Id() != null) {
            TermQueryBuilder catalogId = QueryBuilders.termQuery("catalogId", param.getCatalog3Id());
            boolQueryBuilder.filter().add(catalogId);
        }

        if (param.getBrandIds() != null && param.getBrandIds().size() > 0) {
            TermsQueryBuilder brandIds = QueryBuilders.termsQuery("catalogId", param.getBrandIds());
            boolQueryBuilder.filter().add(brandIds);
        }

        if (param.getHasStock() != null) {
            TermQueryBuilder hasStock;
//            System.out.println(param.getHasStock());
            if (param.getHasStock() == 0) {
                hasStock = QueryBuilders.termQuery("hasStock", false);
            } else {
                hasStock = QueryBuilders.termQuery("hasStock", true);
            }
            boolQueryBuilder.filter().add(hasStock);
        }

        if (!StringUtils.isEmpty(param.getSkuPrice())) {
            RangeQueryBuilder skuPrice = QueryBuilders.rangeQuery("skuPrice");
            //-500 返会长度为2的数组第一个元素为空
            //500- 只会返回长度为1的数组 元素是500
            String[] split = param.getSkuPrice().split("-");
            if (split.length == 2) {
                if (!StringUtils.isEmpty(split[0])) {
                    skuPrice.gte(split[0]);
                }
                if (!StringUtils.isEmpty(split[1])) {
                    skuPrice.lte(split[1]);
                }
            } else {
                skuPrice.gte(split[0]);
            }

            boolQueryBuilder.filter().add(skuPrice);
        }


        //nested filter
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {
            /**
             * 按照属性筛选
             * attr=系统(属性ID)-windows:ios:android:linux
             * attr=尺寸(属性ID)-12.1:13.5:14.2:15.6
             */
            param.getAttrs().forEach(attr -> {
                // 注意 ： attrs 里面 只要有1个属性符合查询条件attrId attrValue 的值 就会被查询
                //而且每个attr 就是一个 nested filter 多个 boolNestedQueryBuilder nested filter
                //每一个属性之间都是并 关系  属性限制越多  nested filter  结果越精确
                BoolQueryBuilder boolNestedQueryBuilder = QueryBuilders.boolQuery();
                String[] attrIds = attr.split("-");
                String[] values = attrIds[1].split(":");
                TermQueryBuilder attrId = QueryBuilders.termQuery("attrs.attrId", attrIds[0]);
                TermsQueryBuilder attrValue = QueryBuilders.termsQuery("attrs.attrValue", values);
                boolNestedQueryBuilder.must().add(attrId);
                boolNestedQueryBuilder.must().add(attrValue);
                NestedQueryBuilder attrs = QueryBuilders.nestedQuery("attrs", boolNestedQueryBuilder, ScoreMode.None);
                boolQueryBuilder.filter().add(attrs);
            });
        }

        searchSourceBuilder.query(boolQueryBuilder);
        //排序
        /**
         *
         *  sort=saleCount-asc/desc
         *  sort=hotScore-asc/desc   综合排序（热度评分）
         *  sort=skuPrice-asc/desc
         */
        if (!StringUtils.isEmpty(param.getSort())) {
            String[] split = param.getSort().split("-");
//            searchSourceBuilder.sort(split[0], split[1].equalsIgnoreCase("desc") ? SortOrder.DESC : SortOrder.ASC);
            searchSourceBuilder.sort(split[0], SortOrder.fromString(split[1]));
        }

        //高亮
        //传了keyword 才高亮
        if (!StringUtils.isEmpty(param.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle").preTags("<b style='color:red'>").postTags("</b>");
            searchSourceBuilder.highlighter(highlightBuilder);
        }

        //分页
        //from=(pageNum-1)*pageSize
        int from = (param.getPageNum() - 1) * EsConstant.PRODUCT_PAGESIZE;
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);

        //=======================================聚合============================================

        //构造聚合brandAgg
        TermsAggregationBuilder brandAgg = AggregationBuilders.terms("brand_agg").field("brandId").size(10);
        brandAgg.subAggregation(AggregationBuilders.terms("brand_name__agg").field("brandName").size(1));
        brandAgg.subAggregation(AggregationBuilders.terms("brand_image__agg").field("brandImg").size(1));
        searchSourceBuilder.aggregation(brandAgg);
        //构造聚合catalog_agg
        TermsAggregationBuilder catalogAgg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(50);
        catalogAgg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        searchSourceBuilder.aggregation(catalogAgg);

        //构造nested 聚合attr_agg size 可以写成1
        NestedAggregationBuilder nestedAggregationBuilder = new NestedAggregationBuilder("attr_agg", "attrs");
        TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId").size(50);
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        //attrValue 值有多个
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));
        nestedAggregationBuilder.subAggregation(attrIdAgg);
        searchSourceBuilder.aggregation(nestedAggregationBuilder);

        System.out.println(searchSourceBuilder);

//        searchRequest.source(searchSourceBuilder);
        // 1 创建检索请求 参数为要检索的索引
        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, searchSourceBuilder);

        //=============================构造查询返回结果=====================================
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, ElasticSearchConfig.COMMON_OPTIONS);
        //构造返回数据
        SearchResult searchResult = new SearchResult();

        //外层outerHits信息
        SearchHits outerHits = searchResponse.getHits();

        //总记录数
        long total = outerHits.getTotalHits().value;
        searchResult.setTotal(total);

        //总页数 计算
        Integer pageSize = EsConstant.PRODUCT_PAGESIZE;
        // (total+pageSize-1)/pageSize  计算总页数  (10+5-1)/5
        //Long totalPages =(long) Math.ceil((double)total / pageSize);
        Long totalPages = (total + pageSize - 1) / pageSize;
        searchResult.setTotalPages(totalPages);

        //当前页码
        Integer pageNum = param.getPageNum();
        searchResult.setPageNum(pageNum);

        //内层innerHit获取的信息
        SearchHit[] innerHits = outerHits.getHits();
        if (innerHits != null && innerHits.length > 0) {
            //获取product list
            List<SkuEsModel> skuEsModels = new ArrayList<>();
            for (SearchHit innerHit : innerHits) {
                String sourceAsString = innerHit.getSourceAsString();
                SkuEsModel skuEsModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
                //换成高亮的skuTitle
                if (!StringUtils.isEmpty(param.getKeyword())) {
                    skuEsModel.setSkuTitle(innerHit.getHighlightFields().get("skuTitle").getFragments()[0].string());
                }
                skuEsModels.add(skuEsModel);
            }
            searchResult.setProducts(skuEsModels);
        }

        //================聚合中获取的信息=======================
        Aggregations aggregations = searchResponse.getAggregations();

        //当前查询到的结果所有涉及到所有分类
        //AggregationBuilders.terms  转成terms buckets
        ParsedTerms catalog_agg = aggregations.get("catalog_agg");
        List<? extends Terms.Bucket> catalogAggBuckets = catalog_agg.getBuckets();
        List<SearchResult.CatalogVo> catalogVos = catalogAggBuckets.stream().map(catalogAggBucket -> {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            long key = catalogAggBucket.getKeyAsNumber().longValue();
            catalogVo.setCatalogId(key);
            ParsedStringTerms catalog_name_agg = catalogAggBucket.getAggregations().get("catalog_name_agg");
            String catalogName = catalog_name_agg.getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogName(catalogName);
            return catalogVo;
        }).collect(Collectors.toList());
        searchResult.setCatalogs(catalogVos);


        //当前查询到的结果所有涉及到的品牌
        ParsedTerms brand_agg = aggregations.get("brand_agg");
        List<? extends Terms.Bucket> brandAggBuckets = brand_agg.getBuckets();
        List<SearchResult.BrandVo> brandVos = brandAggBuckets.stream().map(brandAggBucket -> {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            long key = brandAggBucket.getKeyAsNumber().longValue();
            brandVo.setBrandId(key);
            Terms brand_name__agg = brandAggBucket.getAggregations().get("brand_name__agg");
            String brand_name = brand_name__agg.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandName(brand_name);
            Terms brand_image__agg = brandAggBucket.getAggregations().get("brand_image__agg");
            String brand_image = brand_image__agg.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandImg(brand_image);
            return brandVo;
        }).collect(Collectors.toList());
        searchResult.setBrands(brandVos);

        //当前查询到的结果所有涉及到的所有属性
        ParsedNested attr_agg = aggregations.get("attr_agg");
        ParsedLongTerms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");
        List<? extends Terms.Bucket> attrAggBuckets = attr_id_agg.getBuckets();
        List<SearchResult.AttrVo> attrVos = attrAggBuckets.stream().map(attr -> {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            long key = attr.getKeyAsNumber().longValue();
            attrVo.setAttrId(key);
            ParsedStringTerms attr_name_agg = attr.getAggregations().get("attr_name_agg");
            String attr_name = attr_name_agg.getBuckets().get(0).getKeyAsString();
            attrVo.setAttrName(attr_name);
            ParsedStringTerms attr_value_agg = attr.getAggregations().get("attr_value_agg");
            //TODO attr value 可以 有多个  ; 分开？？？？
            List<String> attr_value = attr_value_agg.getBuckets().stream().map(value -> {
                String valueString = ((Terms.Bucket) value).getKeyAsString();
                return valueString;
            }).collect(Collectors.toList());
            attrVo.setAttrValues(attr_value);
            return attrVo;
        }).collect(Collectors.toList());
        searchResult.setAttrs(attrVos);

        //面包屑导航
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {
            List<SearchResult.NavVo> navs = new ArrayList<>();
            param.getAttrs().forEach(attr -> {
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                String[] attrIds = attr.split("-");
                navVo.setNavValue(attrIds[1]);
                searchResult.getAttrs().forEach(attrVo -> {
                    if (attrVo.getAttrId().toString().equals(attrIds[0])) {
                        navVo.setNavName(attrVo.getAttrName());
                    }
                });
                String value = null;
                try {
                    value = URLEncoder.encode(attrIds[1], "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                String url = param.getUrl();
                String attrs="&attrs=" + attrIds[0] + "-" + value;
                String s =null;
                if(url.indexOf(attrs)>0){
                    s=url.replace("&attrs=" + attrIds[0] + "-" + value, "");
                }else{
                    s=url.replace("attrs=" + attrIds[0] + "-" + value, "");
                }
                navVo.setLink(s);
                navs.add(navVo);
            });
            searchResult.setNavs(navs);
        }
        System.out.println(searchResult);
        return searchResult;
    }
}
