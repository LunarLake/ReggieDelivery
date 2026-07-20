package com.wyc.reggie;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wyc.reggie.common.BaseContext;
import com.wyc.reggie.entity.Employee;
import com.wyc.reggie.service.EmployeeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.DigestUtils;

@SpringBootTest
public class TestEmpService {

    @Autowired
    private EmployeeService employeeService;

    @BeforeEach
    void setUp() {
        BaseContext.setCurrentId(1766200000000000001L);
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Employee::getUsername, "test_crud2");
        employeeService.remove(wrapper);
    }

    @AfterEach
    void tearDown() {
        BaseContext.remove();
    }

    @Test
    public void testInsert() {
        Employee e = new Employee();
        e.setName("测试员工2");
        e.setUsername("test_crud2");
        e.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        e.setPhone("13800002222");
        e.setSex("1");
        e.setIdNumber("110101200001010022");
        e.setStatus(1);
        employeeService.save(e);
    }
}
