package com.phoenixhell.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.phoenixhell.common.valid.AddGroup;
import com.phoenixhell.common.valid.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.io.Serializable;

/**
 * 属性分组
 * 
 * @author phoenixhell
 * @email phoenixrever@gmail.com
 * @date 2021-05-13 23:21:46
 */
@Data
@TableName("pms_attr_group")
public class AttrGroupEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 分组id
	 */
	@NotNull(message = "修改必须要指定ID",groups = UpdateGroup.class)
	@Null(message = "增加不需要指定ID", groups = AddGroup.class)
	@TableId
	private Long attrGroupId;
	/**
	 * 组名
	 */
	@NotBlank(message = "品牌名不能为空",groups = {AddGroup.class,UpdateGroup.class})
	private String attrGroupName;
	/**
	 * 排序
	 */
	@NotNull(message = "新增不能为空",groups = {UpdateGroup.class,AddGroup.class})//integer 不能用notEmpty
	@Min(value = 0, message = "排序必须大于等于0",groups = {UpdateGroup.class,AddGroup.class})
	private Integer sort;
	/**
	 * 描述
	 */
	private String descript;
	/**
	 * 组图标
	 */
	private String icon;
	/**
	 * 所属分类id
	 */
	@NotNull(message = "修改必须要指定ID", groups = {UpdateGroup.class, AddGroup.class})
	private Long catalogId;

	@TableField(exist = false)
	private Long[] catalogPath;
}
