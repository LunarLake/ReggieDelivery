package com.wyc.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wyc.reggie.common.R;
import com.wyc.reggie.entity.Category;
import com.wyc.reggie.service.CategoryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    //添加分类
    @PostMapping
    public R<String> save(@RequestBody Category category, HttpServletRequest request) {

        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        Long id = (Long) request.getSession().getAttribute("employee");
        category.setCreateUser(id);
        category.setUpdateUser(id);
        categoryService.save(category);
        return R.success("新增分类成功");
    }

    //分页显示
    @GetMapping("/page")
    public R<Page<Category>> page(int page, int pageSize, String name) {
        Page<Category> pageInfo = new Page<>(page, pageSize);   // 创建分页构造器
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();  // 创建条件构造器
        wrapper.like(name != null && !name.isEmpty(), Category::getName, name);// 模糊查询分类名称
        wrapper.orderByDesc(Category::getUpdateTime);// 按更新时间降序排序
        categoryService.page(pageInfo, wrapper);// 调用service查询分页数据
        return R.success(pageInfo);
    }

}
