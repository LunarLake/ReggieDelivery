package com.wyc.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wyc.reggie.entity.Dish;
import com.wyc.reggie.entity.DishFlavor;
import com.wyc.reggie.mapper.DishMapper;
import com.wyc.reggie.service.DishFlavorService;
import com.wyc.reggie.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    @Override
    @Transactional
    public void saveWithFlavor(Dish dish, List<DishFlavor> flavors) {
        // 保存菜品，Snowflake 自动生成 id
        this.save(dish);

        // 设置 dishId 并批量保存口味
        Long dishId = dish.getId();
        for (DishFlavor flavor : flavors) {
            flavor.setDishId(dishId);
        }
        dishFlavorService.saveBatch(flavors);
    }

    @Override
    @Transactional
    public void updateWithFlavor(Dish dish, List<DishFlavor> flavors) {
        // 更新菜品基本信息
        this.updateById(dish);

        // 删除原有口味
        LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DishFlavor::getDishId, dish.getId());
        dishFlavorService.remove(wrapper);

        // 插入新口味
        Long dishId = dish.getId();
        for (DishFlavor flavor : flavors) {
            flavor.setDishId(dishId);
        }
        dishFlavorService.saveBatch(flavors);
    }

    @Override
    public Dish getByIdWithFlavor(Long id) {
        // 查询菜品基本信息
        Dish dish = this.getById(id);
        if (dish == null) {
            return null;
        }

        // 查询关联口味并设置到 dish
        LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DishFlavor::getDishId, id);
        List<DishFlavor> flavors = dishFlavorService.list(wrapper);
        dish.setFlavors(flavors);
        return dish;
    }

    @Override
    @Transactional
    public void removeWithFlavor(List<Long> ids) {
        // 批量删除菜品
        this.removeByIds(ids);

        // 批量删除关联口味
        LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(DishFlavor::getDishId, ids);
        dishFlavorService.remove(wrapper);
    }
}
