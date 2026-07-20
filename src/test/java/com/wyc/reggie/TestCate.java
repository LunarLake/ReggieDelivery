package com.wyc.reggie;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wyc.reggie.common.BaseContext;
import com.wyc.reggie.entity.Category;
import com.wyc.reggie.mapper.CategoryMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestCate {
    @Autowired
    private CategoryMapper categoryMapper;

    @BeforeEach
    void setUp() {
        BaseContext.setCurrentId(1766200000000000001L);
        // 清理可能残留的测试数据
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getName, "测试分类");
        categoryMapper.delete(wrapper);
    }

    @AfterEach
    void tearDown() {
        BaseContext.remove();
    }

    @Test
    void testInsert() {
        Category c = new Category();
        c.setName("测试分类");
        c.setType(1);
        c.setSort(10);
        categoryMapper.insert(c);
    }

}
