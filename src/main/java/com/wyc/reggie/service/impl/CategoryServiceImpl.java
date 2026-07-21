package com.wyc.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wyc.reggie.common.AppException;
import com.wyc.reggie.entity.Category;
import com.wyc.reggie.entity.Dish;
import com.wyc.reggie.entity.Setmeal;
import com.wyc.reggie.mapper.CategoryMapper;
import com.wyc.reggie.service.CategoryService;
import com.wyc.reggie.service.DishService;
import com.wyc.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category>
        implements CategoryService {
    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;

    // 分类删除方法
    @Override
    public void remove(Long id) {
        // 检查该分类下是否有关联菜品
        LambdaQueryWrapper<Dish> dishWrapper = new LambdaQueryWrapper<>();
        dishWrapper.eq(Dish::getCategoryId, id);
        long dishCount = dishService.count(dishWrapper);

        // 检查该分类下是否有关联套餐
        LambdaQueryWrapper<Setmeal> setmealWrapper = new LambdaQueryWrapper<>();
        setmealWrapper.eq(Setmeal::getCategoryId, id);
        long setmealCount = setmealService.count(setmealWrapper);

        // 有关联数据则不允许删除
        if (dishCount > 0 || setmealCount > 0) {
            throw new AppException("该分类下存在菜品或套餐，无法删除");
        }
        super.removeById(id);
    }
}
