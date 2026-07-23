package com.wyc.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.wyc.reggie.common.BaseContext;
import com.wyc.reggie.common.R;
import com.wyc.reggie.entity.AddressBook;
import com.wyc.reggie.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 地址簿管理 — 移动端消费者收货地址
 */
@Slf4j
@RestController
@RequestMapping("/addressBook")
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    /** 获取当前用户所有地址 */
    @GetMapping("/list")
    public R<List<AddressBook>> list() {
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<AddressBook> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AddressBook::getUserId, userId);
        wrapper.orderByDesc(AddressBook::getIsDefault).orderByDesc(AddressBook::getUpdateTime);
        return R.success(addressBookService.list(wrapper));
    }

    /** 获取最近更新的地址 */
    @GetMapping("/lastUpdate")
    public R<AddressBook> lastUpdate() {
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<AddressBook> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AddressBook::getUserId, userId);
        wrapper.orderByDesc(AddressBook::getUpdateTime);
        wrapper.last("LIMIT 1");
        return R.success(addressBookService.getOne(wrapper));
    }

    /** 新增地址 */
    @PostMapping
    public R<AddressBook> save(@RequestBody AddressBook addressBook) {
        Long userId = BaseContext.getCurrentId();
        addressBook.setUserId(userId);

        // 如果是该用户第一个地址，自动设为默认
        long count = addressBookService.count(new LambdaQueryWrapper<AddressBook>()
                .eq(AddressBook::getUserId, userId));
        if (count == 0) {
            addressBook.setIsDefault(1);
        }

        addressBookService.save(addressBook);
        return R.success(addressBook);
    }

    /** 修改地址 */
    @PutMapping
    public R<String> update(@RequestBody AddressBook addressBook) {
        addressBookService.updateById(addressBook);
        return R.success("地址修改成功");
    }

    /** 删除地址 */
    @DeleteMapping
    public R<String> delete(Long ids) {
        addressBookService.removeById(ids);
        return R.success("地址删除成功");
    }

    /** 根据 id 查询单个地址 */
    @GetMapping("/{id}")
    public R<AddressBook> getById(@PathVariable Long id) {
        return R.success(addressBookService.getById(id));
    }

    /** 设为默认地址 */
    @PutMapping("/default")
    public R<String> setDefault(@RequestBody AddressBook addressBook) {
        Long userId = BaseContext.getCurrentId();

        // 先将该用户所有地址取消默认
        LambdaUpdateWrapper<AddressBook> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AddressBook::getUserId, userId);
        wrapper.set(AddressBook::getIsDefault, 0);
        addressBookService.update(wrapper);

        // 再将指定地址设为默认
        AddressBook target = new AddressBook();
        target.setId(addressBook.getId());
        target.setIsDefault(1);
        addressBookService.updateById(target);

        return R.success("默认地址设置成功");
    }

    /** 获取默认地址 */
    @GetMapping("/default")
    public R<AddressBook> getDefault() {
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<AddressBook> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AddressBook::getUserId, userId);
        wrapper.eq(AddressBook::getIsDefault, 1);
        return R.success(addressBookService.getOne(wrapper));
    }
}
