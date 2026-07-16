package com.wyc.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wyc.reggie.common.R;
import com.wyc.reggie.entity.Employee;
import com.wyc.reggie.service.EmployeeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    // 员工登录
    @RequestMapping("/login")// 登录接口
    public R<Employee> login(HttpServletRequest request,        // 保存会话状态
                             @RequestBody Employee employee) {  // 接收前端传来的 JSON数据并转换为 Employee对象
        String username = employee.getUsername();
        String password = employee.getPassword();
        String md5pwd=DigestUtils.md5DigestAsHex(password.getBytes());
        // 创建查询对象对应的用户名和员工对象
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Employee::getUsername, username);
        // 调用service完成登录
        Employee emp = employeeService.getOne(wrapper);
        // 如果没有查询到则返回登录失败结果
        if (emp == null) {
            return R.error("登录失败");
        }
        // 密码比对，如果不一致则返回登录失败结果
        if (!emp.getPassword().equals(md5pwd)) {
            return R.error("登录失败");
        }
        // 查看员工状态，如果为已禁用状态，则返回员工已禁用结果
        if (emp.getStatus() == 0) {
            return R.error("账号已禁用");
        }
        // 登录成功，将员工id存入Session并返回登录成功结果
        request.getSession().invalidate();
        HttpSession session = request.getSession();
        session.setAttribute("employee", emp.getId());
        return R.success(emp);
    }

    // 员工退出
    @RequestMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        // 清理 Session中保存的当前登录员工的 id
        request.getSession().invalidate();
        return R.success("退出成功");
    }

    // 员工分页查询
    @GetMapping("/page")
    public R<Page<Employee>> page(@RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "10") int pageSize,
                                   @RequestParam(required = false) String name) {
        // 构建查询条件：按姓名模糊搜索，按更新时间降序排列
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.trim().isEmpty()) {
            wrapper.like(Employee::getName, name);
        }
        wrapper.orderByDesc(Employee::getUpdateTime);

        // 执行分页查询
        Page<Employee> pageResult = employeeService.page(new Page<>(page, pageSize), wrapper);

        // 脱敏：移除密码字段
        if (pageResult.getRecords() != null) {
            pageResult.getRecords().forEach(emp -> emp.setPassword(null));
        }

        return R.success(pageResult);
    }
}
