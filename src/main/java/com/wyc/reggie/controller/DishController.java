package com.wyc.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wyc.reggie.common.R;
import com.wyc.reggie.entity.Category;
import com.wyc.reggie.entity.Dish;
import com.wyc.reggie.entity.DishFlavor;
import com.wyc.reggie.service.CategoryService;
import com.wyc.reggie.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增菜品
     */
    @PostMapping
    public R<String> save(@RequestBody Dish dish) {
        List<DishFlavor> flavors = dish.getFlavors();
        dishService.saveWithFlavor(dish, flavors != null ? flavors : List.of());
        return R.success("新增菜品成功");
    }

    /**
     * 分页查询菜品
     */
    @GetMapping("/page")
    public R<Page<Dish>> page(int page, int pageSize, String name) {
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(name != null && !name.isEmpty(), Dish::getName, name);
        wrapper.orderByDesc(Dish::getUpdateTime);
        dishService.page(pageInfo, wrapper);

        // 批量查询分类名称，避免 N+1 问题
        Set<Long> categoryIds = pageInfo.getRecords().stream()
                .map(Dish::getCategoryId)
                .collect(Collectors.toSet());
        if (!categoryIds.isEmpty()) {
            List<Category> categories = categoryService.listByIds(categoryIds);
            Map<Long, String> categoryNameMap = categories.stream()
                    .collect(Collectors.toMap(Category::getId, Category::getName));
            for (Dish dish : pageInfo.getRecords()) {
                dish.setCategoryName(categoryNameMap.get(dish.getCategoryId()));
            }
        }
        return R.success(pageInfo);
    }

    /**
     * 根据id查询菜品及口味
     */
    @GetMapping("/{id}")
    public R<Dish> getById(@PathVariable Long id) {
        Dish dish = dishService.getByIdWithFlavor(id);
        return R.success(dish);
    }

    /**
     * 修改菜品
     */
    @PutMapping
    public R<String> update(@RequestBody Dish dish) {
        List<DishFlavor> flavors = dish.getFlavors();
        dishService.updateWithFlavor(dish, flavors != null ? flavors : List.of());
        return R.success("菜品修改成功");
    }

    /**
     * 批量删除菜品
     */
    @DeleteMapping
    public R<String> delete(String ids) {
        List<Long> idList = Arrays.stream(ids.split(","))
                .map(String::trim)
                .map(Long::valueOf)
                .collect(Collectors.toList());
        dishService.removeWithFlavor(idList);
        return R.success("菜品删除成功");
    }

    /**
     * 批量启售/停售
     */
    @PostMapping("/status/{status}")
    public R<String> batchStatus(@PathVariable Integer status, String ids) {
        List<Long> idList = Arrays.stream(ids.split(","))
                .map(String::trim)
                .map(Long::valueOf)
                .collect(Collectors.toList());
        LambdaUpdateWrapper<Dish> wrapper = new LambdaUpdateWrapper<>();
        wrapper.in(Dish::getId, idList);
        wrapper.set(Dish::getStatus, status);
        dishService.update(wrapper);
        return R.success("菜品状态修改成功");
    }

    /**
     * 菜品列表（用于套餐添加菜品弹窗）
     */
    @GetMapping("/list")
    public R<List<Dish>> list(Long categoryId, String name) {
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(categoryId != null, Dish::getCategoryId, categoryId);
        wrapper.like(name != null && !name.isEmpty(), Dish::getName, name);
        wrapper.orderByDesc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(wrapper);
        return R.success(list);
    }
}
