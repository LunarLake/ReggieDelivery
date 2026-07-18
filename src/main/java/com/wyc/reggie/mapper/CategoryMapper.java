package com.wyc.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wyc.reggie.entity.Category;
import org.apache.ibatis.annotations.Mapper;

// 菜品分类的 Mapper接口
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
