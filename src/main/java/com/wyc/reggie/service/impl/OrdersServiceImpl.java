package com.wyc.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wyc.reggie.common.BaseContext;
import com.wyc.reggie.entity.*;
import com.wyc.reggie.mapper.OrdersMapper;
import com.wyc.reggie.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private UserService userService;

    @Override
    @Transactional
    public void submit(Orders orders) {
        Long userId = BaseContext.getCurrentId();

        // 查询当前用户购物车
        LambdaQueryWrapper<ShoppingCart> cartWrapper = new LambdaQueryWrapper<>();
        cartWrapper.eq(ShoppingCart::getUserId, userId);
        List<ShoppingCart> cartList = shoppingCartService.list(cartWrapper);
        if (cartList.isEmpty()) {
            throw new RuntimeException("购物车为空，无法下单");
        }

        // 查询地址信息
        AddressBook addressBook = addressBookService.getById(orders.getAddressBookId());
        if (addressBook == null) {
            throw new RuntimeException("收货地址不存在");
        }

        // 查询用户信息
        User user = userService.getById(userId);

        // 计算总金额
        AtomicInteger amount = new AtomicInteger();
        BigDecimal total = cartList.stream()
                .map(item -> {
                    BigDecimal itemTotal = item.getAmount().multiply(new BigDecimal(item.getNumber()));
                    amount.addAndGet(item.getNumber());
                    return itemTotal;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 构建订单
        orders.setId(null); // 让MyBatis-Plus自动生成Snowflake ID
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setUserId(userId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(1); // 待付款
        orders.setAmount(total);
        orders.setPhone(addressBook.getPhone());
        orders.setAddress(addressBook.getDetail());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserName(user != null ? user.getName() : null);

        this.save(orders);

        // 构建订单明细
        Long orderId = orders.getId();
        List<OrderDetail> details = cartList.stream()
                .map(cart -> {
                    OrderDetail detail = new OrderDetail();
                    detail.setName(cart.getName());
                    detail.setImage(cart.getImage());
                    detail.setOrderId(orderId);
                    detail.setDishId(cart.getDishId());
                    detail.setSetmealId(cart.getSetmealId());
                    detail.setDishFlavor(cart.getDishFlavor());
                    detail.setNumber(cart.getNumber());
                    detail.setAmount(cart.getAmount());
                    return detail;
                })
                .toList();
        orderDetailService.saveBatch(details);

        // 清空购物车
        shoppingCartService.remove(cartWrapper);
    }
}
