package com.phoenixhell.gulimall.product.vo;

import lombok.Data;

import java.util.List;

@Data
public class Catalog2Vo {
    private String id;  //1级父类分类ID
    private String name;
    private String catalog1Id;
    private List<Catalog3Vo> catalog3List; //三级子分类

    @Data
    public static class Catalog3Vo{
        private String catalog2Id;  //二级分类ID
        private String id;  //二级分类ID
        private String name;  //二级分类ID
    }
}
