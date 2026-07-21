package com.wyc.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wyc.reggie.common.AppException;
import com.wyc.reggie.common.R;
import com.wyc.reggie.entity.Category;
import com.wyc.reggie.entity.Dish;
import com.wyc.reggie.entity.Setmeal;
import com.wyc.reggie.service.CategoryService;
import com.wyc.reggie.service.DishService;
import com.wyc.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    //添加分类
    @PostMapping
    public R<String> save(@RequestBody Category category) {
        try {
            categoryService.save(category);
        } catch (Exception e) {
            throw new AppException("类别名重复，新增分类失败");
        }
        return R.success("新增分类成功");
    }

    //分页显示
    @GetMapping("/page")
    public R<Page<Category>> page(int page, int pageSize) {
        Page<Category> pageInfo = new Page<>(page, pageSize);   // 创建分页构造器
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();  // 创建条件构造器
        wrapper.orderByAsc(Category::getSort);// 按 Sort字段升序排序
        categoryService.page(pageInfo, wrapper);// 调用 service查询分页数据
        return R.success(pageInfo);
    }

    //删除分类
    @DeleteMapping
    public R<String> delete(Long id) {
        categoryService.remove(id);
        return R.success("分类删除成功");
    }

    //修改分类
    @PutMapping
    public R<String> update(@RequestBody Category category) {
        categoryService.updateById(category);
        return R.success("分类修改成功");
    }
}
