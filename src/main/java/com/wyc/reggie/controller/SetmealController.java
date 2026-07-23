package com.wyc.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wyc.reggie.common.AppException;
import com.wyc.reggie.common.R;
import com.wyc.reggie.dto.SetmealDto;
import com.wyc.reggie.entity.Category;
import com.wyc.reggie.entity.Setmeal;
import com.wyc.reggie.entity.SetmealDish;
import com.wyc.reggie.service.CategoryService;
import com.wyc.reggie.service.SetmealDishService;
import com.wyc.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    /** 新增套餐 */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        setmealService.saveWithDishes(setmealDto);
        return R.success("新增套餐成功");
    }

    /** 分页查询 — 返回 SetmealDto 携带分类名称 */
    @GetMapping("/page")
    public R<Page<SetmealDto>> page(int page, int pageSize, String name) {
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Setmeal> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(name != null && !name.isEmpty(), Setmeal::getName, name);
        wrapper.orderByDesc(Setmeal::getUpdateTime);
        setmealService.page(pageInfo, wrapper);

        // 批量查分类名称
        Set<Long> categoryIds = pageInfo.getRecords().stream()
                .map(Setmeal::getCategoryId)
                .collect(Collectors.toSet());
        Map<Long, String> categoryNameMap = Map.of();
        if (!categoryIds.isEmpty()) {
            categoryNameMap = categoryService.listByIds(categoryIds).stream()
                    .collect(Collectors.toMap(Category::getId, Category::getName));
        }

        // Setmeal → SetmealDto
        Map<Long, String> nameMap = categoryNameMap;
        List<SetmealDto> dtoRecords = pageInfo.getRecords().stream()
                .map(setmeal -> {
                    SetmealDto dto = new SetmealDto();
                    BeanUtils.copyProperties(setmeal, dto);
                    dto.setCategoryName(nameMap.get(setmeal.getCategoryId()));
                    return dto;
                })
                .toList();

        Page<SetmealDto> dtoPage = new Page<>();
        dtoPage.setCurrent(pageInfo.getCurrent());
        dtoPage.setSize(pageInfo.getSize());
        dtoPage.setTotal(pageInfo.getTotal());
        dtoPage.setRecords(dtoRecords);
        return R.success(dtoPage);
    }

    /** 根据 id 查询 — 返回 SetmealDto 携带套餐菜品列表 */
    @GetMapping("/{id}")
    public R<SetmealDto> getById(@PathVariable Long id) {
        Setmeal setmeal = setmealService.getByIdWithDishes(id);
        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal, setmealDto);
        return R.success(setmealDto);
    }

    /** 更新套餐 */
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto) {
        setmealService.updateWithDishes(setmealDto);
        return R.success("套餐修改成功");
    }

    /** 批量删除 — 启售中套餐不可删除 */
    @DeleteMapping
    public R<String> delete(String ids) {
        List<Long> idList = Arrays.stream(ids.split(","))
                .map(String::trim)
                .map(Long::valueOf)
                .collect(Collectors.toList());

        long count = setmealService.listByIds(idList).stream()
                .filter(s -> s.getStatus() != null && s.getStatus() == 1)
                .count();
        if (count > 0) {
            throw new AppException("选中的套餐中有正在售卖的，请先停售后再删除");
        }

        setmealService.removeWithDishes(idList);
        return R.success("套餐删除成功");
    }

    /** 批量启售/停售 */
    @PostMapping("/status/{status}")
    public R<String> batchStatus(@PathVariable Integer status, String ids) {
        List<Long> idList = Arrays.stream(ids.split(","))
                .map(String::trim)
                .map(Long::valueOf)
                .collect(Collectors.toList());
        LambdaUpdateWrapper<Setmeal> wrapper = new LambdaUpdateWrapper<>();
        wrapper.in(Setmeal::getId, idList);
        wrapper.set(Setmeal::getStatus, status);
        setmealService.update(wrapper);
        return R.success("套餐状态修改成功");
    }

    /** 用户端：按分类查套餐列表 — 仅启售套餐 */
    @GetMapping("/list")
    public R<List<Setmeal>> list(Long categoryId, Integer status) {
        LambdaQueryWrapper<Setmeal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(categoryId != null, Setmeal::getCategoryId, categoryId);
        wrapper.eq(status != null, Setmeal::getStatus, status);
        wrapper.orderByDesc(Setmeal::getUpdateTime);
        return R.success(setmealService.list(wrapper));
    }

    /** 用户端：查套餐内菜品详情 */
    @GetMapping("/dish/{id}")
    public R<List<SetmealDish>> dishDetails(@PathVariable Long id) {
        LambdaQueryWrapper<SetmealDish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SetmealDish::getSetmealId, id);
        wrapper.orderByAsc(SetmealDish::getSort);
        return R.success(setmealDishService.list(wrapper));
    }
}
