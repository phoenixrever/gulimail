package com.phoenixhell.gulimall.search.service;

import com.phoenixhell.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

public interface ProductService {

    Boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException;
}
