package com.wyc.reggie;

import com.wyc.reggie.entity.Employee;
import com.wyc.reggie.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;

@SpringBootTest
public class TestEmpService {

    @Autowired
    private EmployeeService employeeService;

    @Test
    public void testInsert() {
        Employee e=new Employee();
        e.setName("测试员工2");
        e.setUsername("test_crud2");
        e.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        e.setPhone("13800002222");
        e.setSex("1");
        e.setIdNumber("110101200001010022");
        e.setStatus(1);
        e.setCreateTime(LocalDateTime.now());
        e.setUpdateTime(LocalDateTime.now());
        e.setCreateUser(1L);
        e.setUpdateUser(1L);
        employeeService.save(e);
    }
}
