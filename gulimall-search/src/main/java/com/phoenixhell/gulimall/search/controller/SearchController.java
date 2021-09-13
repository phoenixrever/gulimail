package com.phoenixhell.gulimall.search.controller;

import com.phoenixhell.gulimall.search.service.MallSearchService;
import com.phoenixhell.gulimall.search.vo.SearchParam;
import com.phoenixhell.gulimall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Controller
public class SearchController {
    @Autowired
    MallSearchService mallSearchService;

    @GetMapping({"/list.html","/","search.html"})
    public String list(SearchParam param, Model model, HttpServletRequest httpServletRequest) throws IOException {
        //拿到查询string  构建面包屑返回的url
        String queryString = httpServletRequest.getQueryString();
        param.setUrl(queryString);
        System.out.println(param);
        //根据传递来的查询参数去es中检索商品
        SearchResult result = mallSearchService.search(param);
        model.addAttribute("result",result);
        return "index";
    }
}
