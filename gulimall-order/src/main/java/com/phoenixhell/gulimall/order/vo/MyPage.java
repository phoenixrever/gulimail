package com.phoenixhell.gulimall.order.vo;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

public class MyPage<T> extends Page<T> {
    @Override
    public Page<T> setRecords(List<T> records) {
        return super.setRecords(records);
    }
}
