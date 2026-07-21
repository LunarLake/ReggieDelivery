package com.wyc.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wyc.reggie.entity.Category;
// 菜品分类的 Service接口
public interface CategoryService extends IService<Category> {
    // 分类删除方法
    void remove(Long id);
}
