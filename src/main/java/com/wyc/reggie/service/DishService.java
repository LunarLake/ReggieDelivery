package com.wyc.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wyc.reggie.entity.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {

    /** 新增菜品及口味（口味从 dish.getFlavors() 获取） */
    void saveWithFlavor(Dish dish);

    /** 更新菜品及口味（口味从 dish.getFlavors() 获取） */
    void updateWithFlavor(Dish dish);

    /** 根据 id 查询菜品及口味 */
    Dish getByIdWithFlavor(Long id);

    /** 批量删除菜品及口味 */
    void removeWithFlavor(List<Long> ids);
}
