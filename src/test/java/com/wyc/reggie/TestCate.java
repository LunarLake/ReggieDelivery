package com.wyc.reggie;

import com.wyc.reggie.entity.Category;
import com.wyc.reggie.mapper.CategoryMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestCate {
    @Autowired
    private CategoryMapper categoryMapper;

    @Test //添加菜品分类
    void testInsert() {
        Category c = new Category();
        c.setName("测试分类");
        c.setType(1);
        c.setSort(10);
        c.setCreateUser(1L);
        c.setUpdateUser(1L);
        c.setCreateTime(LocalDateTime.now());
        c.setUpdateTime(LocalDateTime.now());
        categoryMapper.insert(c);
    }

}
