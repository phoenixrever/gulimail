package com.phoenixhell.gulimall.search.service;

import com.phoenixhell.gulimall.search.vo.SearchParam;
import com.phoenixhell.gulimall.search.vo.SearchResult;

import java.io.IOException;

public interface MallSearchService {

    SearchResult search(SearchParam param) throws IOException;
}
