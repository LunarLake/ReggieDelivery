package com.wyc.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wyc.reggie.entity.Dish;
import com.wyc.reggie.entity.DishFlavor;

import java.util.List;

public interface DishService extends IService<Dish> {

    /**
     * 新增菜品，同时保存对应的口味数据
     */
    void saveWithFlavor(Dish dish, List<DishFlavor> flavors);

    /**
     * 更新菜品，同时更新对应的口味数据
     */
    void updateWithFlavor(Dish dish, List<DishFlavor> flavors);

    /**
     * 根据id查询菜品及对应的口味数据
     */
    Dish getByIdWithFlavor(Long id);

    /**
     * 批量删除菜品及对应的口味数据
     */
    void removeWithFlavor(List<Long> ids);
}
