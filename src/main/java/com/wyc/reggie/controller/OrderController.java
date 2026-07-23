package com.wyc.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wyc.reggie.common.BaseContext;
import com.wyc.reggie.common.R;
import com.wyc.reggie.entity.OrderDetail;
import com.wyc.reggie.entity.Orders;
import com.wyc.reggie.entity.ShoppingCart;
import com.wyc.reggie.service.OrderDetailService;
import com.wyc.reggie.service.OrdersService;
import com.wyc.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class OrderController {

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private ShoppingCartService shoppingCartService;

    /** 管理端：分页查询订单 */
    @GetMapping("/order/page")
    public R<Page<Orders>> page(int page, int pageSize, String number,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime beginTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(number != null && !number.isEmpty(), Orders::getNumber, number);
        wrapper.gt(beginTime != null, Orders::getOrderTime, beginTime);
        wrapper.lt(endTime != null, Orders::getOrderTime, endTime);
        wrapper.orderByDesc(Orders::getOrderTime);
        ordersService.page(pageInfo, wrapper);
        return R.success(pageInfo);
    }

    /** 查询订单详情（含明细） */
    @GetMapping("/orderDetail/{id}")
    public R<Orders> getDetail(@PathVariable Long id) {
        Orders orders = ordersService.getById(id);
        if (orders != null) {
            LambdaQueryWrapper<OrderDetail> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(OrderDetail::getOrderId, id);
            orders.setOrderDetails(orderDetailService.list(wrapper));
        }
        return R.success(orders);
    }

    /** 管理端：修改订单状态（派送/完成） */
    @PutMapping("/order")
    public R<String> updateStatus(@RequestBody Orders orders) {
        LambdaUpdateWrapper<Orders> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Orders::getId, orders.getId());
        wrapper.set(Orders::getStatus, orders.getStatus());
        ordersService.update(wrapper);
        return R.success("订单状态修改成功");
    }

    /** 用户端：提交订单 */
    @PostMapping("/order/submit")
    @Transactional
    public R<String> submit(@RequestBody Orders orders) {
        ordersService.submit(orders);
        return R.success("下单成功");
    }

    /** 用户端：查询所有订单 */
    @GetMapping("/order/list")
    public R<List<Orders>> list() {
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getUserId, userId);
        wrapper.orderByDesc(Orders::getOrderTime);
        return R.success(ordersService.list(wrapper));
    }

    /** 用户端：用户分页订单（含订单明细） */
    @GetMapping("/order/userPage")
    public R<Page<Orders>> userPage(int page, int pageSize) {
        Long userId = BaseContext.getCurrentId();
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getUserId, userId);
        wrapper.orderByDesc(Orders::getOrderTime);
        ordersService.page(pageInfo, wrapper);

        // 批量加载订单明细
        List<Orders> records = pageInfo.getRecords();
        if (!records.isEmpty()) {
            Set<Long> orderIds = records.stream()
                    .map(Orders::getId)
                    .collect(Collectors.toSet());
            LambdaQueryWrapper<OrderDetail> detailWrapper = new LambdaQueryWrapper<>();
            detailWrapper.in(OrderDetail::getOrderId, orderIds);
            Map<Long, List<OrderDetail>> detailMap = orderDetailService.list(detailWrapper).stream()
                    .collect(Collectors.groupingBy(OrderDetail::getOrderId));
            records.forEach(order -> order.setOrderDetails(detailMap.getOrDefault(order.getId(), List.of())));
        }
        return R.success(pageInfo);
    }

    /** 用户端：再来一单 */
    @PostMapping("/order/again")
    @Transactional
    public R<String> again(@RequestBody Map<String, Long> param) {
        Long orderId = param.get("id");
        Long userId = BaseContext.getCurrentId();

        // 查询原订单明细
        LambdaQueryWrapper<OrderDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderDetail::getOrderId, orderId);
        List<OrderDetail> details = orderDetailService.list(wrapper);

        // 将明细转换为购物车条目
        List<ShoppingCart> cartItems = details.stream()
                .map(detail -> {
                    ShoppingCart cart = new ShoppingCart();
                    cart.setName(detail.getName());
                    cart.setImage(detail.getImage());
                    cart.setUserId(userId);
                    cart.setDishId(detail.getDishId());
                    cart.setSetmealId(detail.getSetmealId());
                    cart.setDishFlavor(detail.getDishFlavor());
                    cart.setNumber(detail.getNumber());
                    cart.setAmount(detail.getAmount());
                    cart.setCreateTime(LocalDateTime.now());
                    return cart;
                })
                .toList();
        shoppingCartService.saveBatch(cartItems);
        return R.success("再来一单成功");
    }
}
