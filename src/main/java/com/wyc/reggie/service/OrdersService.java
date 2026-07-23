package com.wyc.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wyc.reggie.entity.Orders;

public interface OrdersService extends IService<Orders> {

    /** 提交订单：从购物车生成订单和明细，清空购物车 */
    void submit(Orders orders);
}
