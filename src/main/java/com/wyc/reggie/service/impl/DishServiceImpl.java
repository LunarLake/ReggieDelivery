package com.wyc.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wyc.reggie.entity.Dish;
import com.wyc.reggie.entity.DishFlavor;
import com.wyc.reggie.mapper.DishMapper;
import com.wyc.reggie.service.DishFlavorService;
import com.wyc.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.List;

@Slf4j
@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Value("${reggie.upload.path}")
    private String basePath;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Override
    @Transactional
    public void saveWithFlavor(Dish dish) {
        this.save(dish);

        List<DishFlavor> flavors = dish.getFlavors();
        Long dishId = dish.getId();
        for (DishFlavor flavor : flavors) {
            flavor.setDishId(dishId);
        }
        dishFlavorService.saveBatch(flavors);
    }

    @Override
    @Transactional
    public void updateWithFlavor(Dish dish) {
        List<DishFlavor> flavors = dish.getFlavors();
        // 如果换了图片，删除旧图片文件
        String newImage = dish.getImage();
        if (newImage != null && !newImage.isBlank()) {
            Dish oldDish = this.getById(dish.getId());
            if (oldDish != null) {
                String oldImage = oldDish.getImage();
                if (oldImage != null && !oldImage.isBlank() && !oldImage.equals(newImage)) {
                    deleteImageFile(oldImage);
                }
            }
        }

        this.updateById(dish);

        LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DishFlavor::getDishId, dish.getId());
        dishFlavorService.remove(wrapper);

        Long dishId = dish.getId();
        for (DishFlavor flavor : flavors) {
            flavor.setDishId(dishId);
        }
        dishFlavorService.saveBatch(flavors);
    }

    @Override
    public Dish getByIdWithFlavor(Long id) {
        Dish dish = this.getById(id);
        if (dish == null) {
            return null;
        }

        LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DishFlavor::getDishId, id);
        List<DishFlavor> flavors = dishFlavorService.list(wrapper);
        dish.setFlavors(flavors);
        return dish;
    }

    @Override
    @Transactional
    public void removeWithFlavor(List<Long> ids) {
        // 删除前收集所有关联的图片文件名
        List<Dish> dishes = this.listByIds(ids);
        List<String> images = dishes.stream()
                .map(Dish::getImage)
                .filter(img -> img != null && !img.isBlank())
                .toList();

        // 删除数据库记录
        this.removeByIds(ids);

        LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(DishFlavor::getDishId, ids);
        dishFlavorService.remove(wrapper);

        // 删除磁盘图片文件
        for (String image : images) {
            deleteImageFile(image);
        }
    }

    /** 删除磁盘上的图片文件（失败仅打日志，不影响主流程） */
    private void deleteImageFile(String filename) {
        File file = new File(basePath, filename);
        if (file.exists()) {
            if (file.delete()) {
                log.debug("已删除过期图片: {}", file.getAbsolutePath());
            } else {
                log.warn("删除图片失败: {}", file.getAbsolutePath());
            }
        }
    }
}
