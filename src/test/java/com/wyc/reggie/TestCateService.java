package com.wyc.reggie;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wyc.reggie.common.BaseContext;
import com.wyc.reggie.entity.Category;
import com.wyc.reggie.service.CategoryService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestCateService {

    @Autowired
    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        BaseContext.setCurrentId(1766200000000000001L);
        // 清理可能残留的测试数据
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getName, "测试套餐分类");
        categoryService.remove(wrapper);
    }

    @AfterEach
    void tearDown() {
        BaseContext.remove();
    }

    @Test
    void testInsertSetmealCategory() {
        Category c = new Category();
        c.setName("测试套餐分类");
        c.setType(2);
        c.setSort(10);
        categoryService.save(c);
    }
}
