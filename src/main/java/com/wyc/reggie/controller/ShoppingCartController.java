package com.wyc.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wyc.reggie.common.BaseContext;
import com.wyc.reggie.common.R;
import com.wyc.reggie.entity.ShoppingCart;
import com.wyc.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /** 查询当前用户购物车 */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list() {
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, userId);
        wrapper.orderByAsc(ShoppingCart::getCreateTime);
        return R.success(shoppingCartService.list(wrapper));
    }

    /** 添加商品到购物车 */
    @PostMapping("/add")
    @Transactional
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, userId);

        // 区分菜品和套餐：通过 dishId 或 setmealId 判断
        Long dishId = shoppingCart.getDishId();
        if (dishId != null) {
            wrapper.eq(ShoppingCart::getDishId, dishId);
            // 同菜品同口味算同一项
            String flavor = shoppingCart.getDishFlavor();
            wrapper.eq(flavor != null, ShoppingCart::getDishFlavor, flavor);
        } else {
            wrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }

        ShoppingCart existing = shoppingCartService.getOne(wrapper);
        if (existing != null) {
            existing.setNumber(existing.getNumber() + 1);
            shoppingCartService.updateById(existing);
            return R.success(existing);
        }

        shoppingCart.setNumber(1);
        shoppingCart.setCreateTime(LocalDateTime.now());
        shoppingCartService.save(shoppingCart);
        return R.success(shoppingCart);
    }

    /** 减少商品数量 */
    @PostMapping("/sub")
    @Transactional
    public R<Map<String, Object>> sub(@RequestBody ShoppingCart param) {
        Long userId = BaseContext.getCurrentId();

        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, userId);
        if (param.getDishId() != null) {
            wrapper.eq(ShoppingCart::getDishId, param.getDishId());
        } else {
            wrapper.eq(ShoppingCart::getSetmealId, param.getSetmealId());
        }

        ShoppingCart item = shoppingCartService.getOne(wrapper);
        int newNumber = 0;
        if (item != null) {
            int current = item.getNumber();
            if (current > 1) {
                item.setNumber(current - 1);
                shoppingCartService.updateById(item);
                newNumber = current - 1;
            } else {
                shoppingCartService.removeById(item.getId());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("number", newNumber);
        return R.success(result);
    }

    /** 清空购物车 */
    @DeleteMapping("/clean")
    public R<String> clean() {
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, userId);
        shoppingCartService.remove(wrapper);
        return R.success("清空购物车成功");
    }
}
