package com.wyc.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wyc.reggie.dto.SetmealDto;
import com.wyc.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {

    /** 新增套餐及关联菜品 */
    void saveWithDishes(SetmealDto dto);

    /** 更新套餐及关联菜品 */
    void updateWithDishes(SetmealDto dto);

    /** 根据 id 查询套餐及关联菜品 */
    Setmeal getByIdWithDishes(Long id);

    /** 批量删除套餐、关联菜品及图片文件 */
    void removeWithDishes(List<Long> ids);
}
