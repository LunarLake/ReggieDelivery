package com.wyc.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wyc.reggie.common.R;
import com.wyc.reggie.entity.Employee;
import com.wyc.reggie.service.EmployeeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

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
        // 将密码字段置为 null，避免返回给前端
        if (pageResult.getRecords() != null) {
            pageResult.getRecords().forEach(emp -> emp.setPassword(null));
        }
        return R.success(pageResult);
    }
    // 更新员工信息（启用、禁用）
    @PutMapping
    public R<String> update(HttpServletRequest request, @RequestBody Employee employee) {

        Long id = (Long) request.getSession().getAttribute("employee");
        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(id);
        employeeService.updateById(employee);

        return R.success("员工信息修改成功");
    }

    //新增员工
    @PostMapping
    public R<String> save(HttpServletRequest request,       // 保存会话状态
                          @RequestBody Employee employee) { // 接收前端传来的 JSON数据并转换为 Employee对象
        //添加员工之前，先查询是否存在相同用户名的员工
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Employee::getUsername, employee.getUsername());
        Employee existingEmployee = employeeService.getOne(wrapper);
        if (existingEmployee != null) {
            return R.error("用户名已存在，请更换！");
        }
        // 设置初始密码为123456，并进行MD5加密
        String initialPassword = "123456";
        String md5pwd = DigestUtils.md5DigestAsHex(initialPassword.getBytes());
        employee.setPassword(md5pwd);
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());
        Long id = (Long) request.getSession().getAttribute("employee");
        employee.setCreateUser(id);
        employee.setUpdateUser(id);
        employeeService.save(employee);
        return R.success("新增员工成功");
    }
}
