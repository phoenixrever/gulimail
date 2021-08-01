/**
  * Copyright 2021 bejson.com 
  */
package com.phoenixhell.gulimall.product.vo;
import lombok.Data;

import java.util.List;

/**
 * Auto-generated: 2021-07-22 22:13:46
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */

@Data
public class SpuSaveVo {

    private String spuName;
    private String spuDescription;
    private Long catalogId;
    private Long brandId;
    private int weight;
    private int publishStatus;
    private List<String> descript;
    private List<String> images;
    private Bounds bounds;
    private List<BaseAttrs> baseAttrs;
    private List<Skus> skus;

}