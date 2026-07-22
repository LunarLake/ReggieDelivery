package com.wyc.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wyc.reggie.common.AppException;
import com.wyc.reggie.common.R;
import com.wyc.reggie.dto.DishDto;
import com.wyc.reggie.entity.Category;
import com.wyc.reggie.entity.Dish;
import com.wyc.reggie.service.CategoryService;
import com.wyc.reggie.service.DishService;
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
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryService categoryService;

    /** 新增菜品 — DishDto.flavors 默认为空 List，无需判空 */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }

    /** 分页查询 — 返回 DishDto 携带分类名称 */
    @GetMapping("/page")
    public R<Page<DishDto>> page(int page, int pageSize, String name) {
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(name != null && !name.isEmpty(), Dish::getName, name);
        wrapper.orderByDesc(Dish::getUpdateTime);
        dishService.page(pageInfo, wrapper);

        // 批量查分类名称
        Set<Long> categoryIds = pageInfo.getRecords().stream()
                .map(Dish::getCategoryId)
                .collect(Collectors.toSet());
        Map<Long, String> categoryNameMap = Map.of();
        if (!categoryIds.isEmpty()) {
            categoryNameMap = categoryService.listByIds(categoryIds).stream()
                    .collect(Collectors.toMap(Category::getId, Category::getName));
        }

        // Dish → DishDto
        Map<Long, String> nameMap = categoryNameMap;
        List<DishDto> dtoRecords = pageInfo.getRecords().stream()
                .map(dish -> {
                    DishDto dto = new DishDto();
                    BeanUtils.copyProperties(dish, dto);
                    dto.setCategoryName(nameMap.get(dish.getCategoryId()));
                    return dto;
                })
                .toList();

        Page<DishDto> dtoPage = new Page<>();
        dtoPage.setCurrent(pageInfo.getCurrent());
        dtoPage.setSize(pageInfo.getSize());
        dtoPage.setTotal(pageInfo.getTotal());
        dtoPage.setRecords(dtoRecords);
        return R.success(dtoPage);
    }

    /** 根据 id 查询 — 返回 DishDto 携带口味列表 */
    @GetMapping("/{id}")
    public R<DishDto> getById(@PathVariable Long id) {
        Dish dish = dishService.getByIdWithFlavor(id);
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish, dishDto);
        return R.success(dishDto);
    }

    /** 更新菜品 — DishDto.flavors 默认为空 List，无需判空 */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        dishService.updateWithFlavor(dishDto);
        return R.success("菜品修改成功");
    }

    /** 批量删除 — 启售中菜品不可删除 */
    @DeleteMapping
    public R<String> delete(String ids) {
        List<Long> idList = Arrays.stream(ids.split(","))
                .map(String::trim)
                .map(Long::valueOf)
                .collect(Collectors.toList());

        long count = dishService.listByIds(idList).stream()
                .filter(d -> d.getStatus() != null && d.getStatus() == 1)
                .count();
        if (count > 0) {
            throw new AppException("选中的菜品中有正在售卖的，请先停售后再删除");
        }

        dishService.removeWithFlavor(idList);
        return R.success("菜品删除成功");
    }

    /** 批量启售/停售 */
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

    /** 菜品列表（套餐弹窗用）— 仅启售菜品 */
    @GetMapping("/list")
    public R<List<DishDto>> list(Long categoryId, String name) {
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(categoryId != null, Dish::getCategoryId, categoryId);
        wrapper.like(name != null && !name.isEmpty(), Dish::getName, name);
        wrapper.eq(Dish::getStatus, 1);
        wrapper.orderByDesc(Dish::getUpdateTime);
        List<DishDto> dtoList = dishService.list(wrapper).stream()
                .map(dish -> {
                    DishDto dto = new DishDto();
                    BeanUtils.copyProperties(dish, dto);
                    return dto;
                })
                .toList();
        return R.success(dtoList);
    }
}
