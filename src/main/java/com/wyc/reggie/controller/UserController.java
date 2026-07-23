package com.wyc.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wyc.reggie.common.R;
import com.wyc.reggie.entity.User;
import com.wyc.reggie.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /** 发送验证码 — 演示版直接返回固定码 */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody Map<String, String> param, HttpSession session) {
        String phone = param.get("phone");
        // 演示环境：验证码固定为 1234，存入 session
        session.setAttribute(phone, "1234");
        log.info("验证码发送至 {} : 1234", phone);
        return R.success("验证码发送成功");
    }

    /** 用户登录 */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map<String, String> param, HttpSession session) {
        String phone = param.get("phone");
        String code = param.get("code");

        // 校验验证码
        Object codeInSession = session.getAttribute(phone);
        if (codeInSession == null || !codeInSession.toString().equals(code)) {
            return R.error("验证码错误");
        }

        // 查 user 表，无则新建
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        User user = userService.getOne(wrapper);

        if (user == null) {
            user = new User();
            user.setPhone(phone);
            user.setStatus(1);
            userService.save(user);
        }

        // 检查用户状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            return R.error("账号已被禁用");
        }

        session.setAttribute("user", user.getId());
        log.info("用户登录成功: phone={}, userId={}", phone, user.getId());
        return R.success(user);
    }

    /** 用户登出 */
    @PostMapping("/loginout")
    public R<String> loginout(HttpSession session) {
        session.removeAttribute("user");
        return R.success("退出成功");
    }
}
