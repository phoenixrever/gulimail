package com.phoenixhell.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.phoenixhell.common.to.es.SkuEsModel;
import com.phoenixhell.gulimall.search.config.ElasticSearchConfig;
import com.phoenixhell.gulimall.search.constant.EsConstant;
import com.phoenixhell.gulimall.search.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service("ProductService")
public class ProductServiceImpl implements ProductService {
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Override
    public Boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException {
        //给ES 建立一个索引index  建立好映射关系mapping PUT product kibana已经操作好了
        BulkRequest bulkRequest = new BulkRequest();
        skuEsModels.forEach(skuEsModel -> {
            IndexRequest indexRequest = new IndexRequest(EsConstant.PRODUCT_INDEX);
            indexRequest.id(skuEsModel.getSkuId().toString());
            String s = JSON.toJSONString(skuEsModel);
            indexRequest.source(s, XContentType.JSON);
            bulkRequest.add(indexRequest);
        });
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, ElasticSearchConfig.COMMON_OPTIONS);

        //错误处理
        boolean b = bulk.hasFailures();
        if (b) {
            List<String> ids = Arrays.asList(bulk.getItems()).stream().map(item -> item.getId()).collect(Collectors.toList());
            log.error("商品上架错误:{}", ids);
        }
        return  !b;
    }
}
