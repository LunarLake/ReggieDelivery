package com.wyc.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wyc.reggie.dto.SetmealDto;
import com.wyc.reggie.entity.Setmeal;
import com.wyc.reggie.entity.SetmealDish;
import com.wyc.reggie.mapper.SetmealMapper;
import com.wyc.reggie.service.SetmealDishService;
import com.wyc.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.List;

@Slf4j
@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Value("${reggie.upload.path}")
    private String basePath;

    @Autowired
    private SetmealDishService setmealDishService;

    @Override
    @Transactional
    public void saveWithDishes(SetmealDto dto) {
        this.save(dto);

        List<SetmealDish> dishes = dto.getSetmealDishes();
        Long setmealId = dto.getId();
        for (SetmealDish dish : dishes) {
            dish.setSetmealId(setmealId);
        }
        setmealDishService.saveBatch(dishes);
    }

    @Override
    @Transactional
    public void updateWithDishes(SetmealDto dto) {
        // 如果换了图片，删除旧图片文件
        String newImage = dto.getImage();
        if (newImage != null && !newImage.isBlank()) {
            Setmeal oldSetmeal = this.getById(dto.getId());
            if (oldSetmeal != null) {
                String oldImage = oldSetmeal.getImage();
                if (oldImage != null && !oldImage.isBlank() && !oldImage.equals(newImage)) {
                    deleteImageFile(oldImage);
                }
            }
        }

        this.updateById(dto);

        // 删除旧关联，插入新关联
        LambdaQueryWrapper<SetmealDish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SetmealDish::getSetmealId, dto.getId());
        setmealDishService.remove(wrapper);

        List<SetmealDish> dishes = dto.getSetmealDishes();
        Long setmealId = dto.getId();
        for (SetmealDish dish : dishes) {
            dish.setSetmealId(setmealId);
        }
        setmealDishService.saveBatch(dishes);
    }

    @Override
    public Setmeal getByIdWithDishes(Long id) {
        Setmeal setmeal = this.getById(id);
        if (setmeal == null) {
            return null;
        }

        LambdaQueryWrapper<SetmealDish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SetmealDish::getSetmealId, id);
        List<SetmealDish> dishes = setmealDishService.list(wrapper);
        setmeal.setSetmealDishes(dishes);
        return setmeal;
    }

    @Override
    @Transactional
    public void removeWithDishes(List<Long> ids) {
        // 删除前收集所有关联的图片文件名
        List<Setmeal> setmeals = this.listByIds(ids);
        List<String> images = setmeals.stream()
                .map(Setmeal::getImage)
                .filter(img -> img != null && !img.isBlank())
                .toList();

        // 删除数据库记录
        this.removeByIds(ids);

        LambdaQueryWrapper<SetmealDish> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(SetmealDish::getSetmealId, ids);
        setmealDishService.remove(wrapper);

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
