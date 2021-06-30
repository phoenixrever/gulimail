package com.phoenixhell.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.phoenixhell.common.utils.PageUtils;
import com.phoenixhell.common.utils.Query;
import com.phoenixhell.gulimall.product.dao.CategoryDao;
import com.phoenixhell.gulimall.product.entity.CategoryEntity;
import com.phoenixhell.gulimall.product.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //查出所有分类
        List<CategoryEntity> list = this.list();
        /*组装树形结构  手动写 不推荐
        List<CategoryEntity> levelOneMenu = list.stream().filter(categoryEntity -> categoryEntity.getParentCid() == 0).collect(Collectors.toList());
        List<CategoryEntity> collect1 = levelOneMenu.stream().map(categoryEntity -> {
            List<CategoryEntity> collect = list.stream().filter(x -> x.getParentCid().equals(categoryEntity.getCatId())).collect(Collectors.toList());
           categoryEntity.setChildren(collect);
            return categoryEntity;
        }).collect(Collectors.toList());
        return collect1;
        */
        //找到所有分类及其子分类 最后一步list为空  menu自然也为null  所欲空指针异常
        List<CategoryEntity> levelOneMenu = list.stream().filter(categoryEntity ->
                categoryEntity.getParentCid() == 0
        ).map(menu -> {
            menu.setChildren(getChildren(menu, list));
            return menu;
        }).sorted((menu1, menu2) -> (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort())).collect(Collectors.toList());
        return levelOneMenu;
    }

    @Override
    public Long[] findCatalogPath(Long catalogId) {
        List<Long> path = new ArrayList<>();
        List<Long> catalogPath = findParent(catalogId, path);
        // catalogPath.toArray();返回的obj 不能强转
        Collections.reverse(catalogPath);
        return catalogPath.toArray(new Long[catalogPath.size()]);
    }

    private List<Long> findParent(Long catalogId,List<Long> path){
        path.add(catalogId);
        CategoryEntity category = this.getById(catalogId);
        if(category.getParentCid()!=0){
            findParent(category.getParentCid(),path);
        }
        return path;
    }
    //递归查找所有子分类  最后一步list为空  menu自然也为null  所欲空指针异常
    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> list) {
        List<CategoryEntity> children = list.stream().filter(menu -> menu.getParentCid().equals(root.getCatId()))
                .map(menu -> {
                    menu.setChildren(getChildren(menu, list));
                    return menu;
                }).sorted((menu1, menu2) -> (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort())).collect(Collectors.toList());

        return children;
    }
}