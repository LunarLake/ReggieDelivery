package com.wyc.reggie;

import com.wyc.reggie.entity.Category;
import com.wyc.reggie.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

@SpringBootTest
public class TestCateService {

    @Autowired
    private CategoryService categoryService;

    @Test //添加套餐分类
    void testInsertSetmealCategory() {
        Category c = new Category();
        c.setName("测试套餐分类");
        c.setType(2);
        c.setSort(10);
        c.setCreateUser(1L);
        c.setUpdateUser(1L);
        c.setCreateTime(LocalDateTime.now());
        c.setUpdateTime(LocalDateTime.now());
        categoryService.save(c);
    }
}
