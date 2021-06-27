package com.phoenixhell.gulimall.product.controller;

import com.phoenixhell.common.utils.PageUtils;
import com.phoenixhell.common.utils.R;
import com.phoenixhell.gulimall.product.entity.CategoryEntity;
import com.phoenixhell.gulimall.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 商品三级分类
 *
 * @author phoenixhell
 * @email phoenixrever@gmail.com
 * @date 2021-05-13 23:21:46
 */
@RestController
@RequestMapping("product/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/list/tree")
    public R listTree() {
        List<CategoryEntity> list = categoryService.listWithTree();
        return R.ok().put("list", list);
    }

    /**
     * 列表
     */
//    @RequestMapping中Get&Post 不写默认为都支持
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = categoryService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{catId}")
    public R info(@PathVariable("catId") Long catId) {
        CategoryEntity category = categoryService.getById(catId);

        return R.ok().put("category", category);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody CategoryEntity category) {
        //TODO  批量save
        System.out.println(category);
        if (category.getCatLevel() <= 3) {
            categoryService.save(category);
        }
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    public R update(@RequestBody CategoryEntity category) {
        System.out.println(category);
        categoryService.updateById(category);

        return R.ok();
    }

    /**
     * 批量修改catlevel和父节点
     */
    @PutMapping("/updateCategories")
    public R update(@RequestBody List<CategoryEntity> categories) {
//        categories.stream().map(x->{
//            System.out.println(x);
//            return x;
//        }).collect(Collectors.toList());
;        List<CategoryEntity> collect = categories.stream().filter(x -> x.getCatLevel()!=null && x.getCatLevel()> 3).collect(Collectors.toList());
        int size = collect.size();
        if (size==0){
            categoryService.updateBatchById(categories);
        }
        return R.ok();
    }

    /**
     * 删除
     *
     * @requestBody 发送post json 请求
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody List<Long> catIds) {
        catIds.forEach(x -> {
            List<CategoryEntity> children = categoryService.getById(x).getChildren();
            if (children!=null && children.size() > 0) {
                throw new RuntimeException("出错啦！");
            }
        });
        categoryService.removeByIds(catIds);
        return R.ok();
    }
}
