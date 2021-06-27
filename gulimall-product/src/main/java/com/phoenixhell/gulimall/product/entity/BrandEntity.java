package com.phoenixhell.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.phoenixhell.common.valid.AddGroup;
import com.phoenixhell.common.valid.ListValue;
import com.phoenixhell.common.valid.UpdateGroup;
import com.phoenixhell.common.valid.UpdateStatusGroup;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;
import java.io.Serializable;

/**
 * 品牌
 *
 * @author phoenixhell
 * @email phoenixrever@gmail.com
 * @date 2021-05-13 23:21:46
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 品牌id
     */
    @NotNull(message = "修改必须要指定ID", groups = {UpdateGroup.class,UpdateStatusGroup.class})
    @Null(message = "增加不需要指定ID", groups = AddGroup.class)
    @TableId
    private Long brandId;
    /**
     * 品牌名
     */
    @NotBlank(message = "品牌名不能为空",groups = {UpdateGroup.class,AddGroup.class})
    private String name;
    /**
     * 品牌logo地址
     */
    @NotEmpty(message = "不能为空",groups = {AddGroup.class})
    @URL(message = "logo必须是一个合法的url地址",groups = {UpdateGroup.class,AddGroup.class})
    private String logo;
    /**
     * 介绍
     */
    private String descript;
    /**
     * 显示状态[0-不显示；1-显示]
     */
    @NotNull(message = "新增不能为空",groups = {AddGroup.class})
    @ListValue(values = {0,1},groups = {AddGroup.class,UpdateGroup.class, UpdateStatusGroup.class})
    private Integer showStatus;
    /**
     * 检索首字母
     */
    @NotEmpty(message = "新增不能为空",groups = {UpdateGroup.class,AddGroup.class})
    @Pattern(regexp = "^[a-zA-Z]$", message = "检索首字母必须是一个字母",groups = {UpdateGroup.class,AddGroup.class})
    private String firstLetter;
    /**
     * 排序
     */
    @NotNull(message = "新增不能为空",groups = {UpdateGroup.class,AddGroup.class})//integer 不能用notEmpty
    @Min(value = 0, message = "排序必须大于等于0",groups = {UpdateGroup.class,AddGroup.class})
    private Integer sort;

}
