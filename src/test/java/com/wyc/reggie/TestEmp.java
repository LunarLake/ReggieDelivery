package com.wyc.reggie;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.wyc.reggie.entity.Employee;
import com.wyc.reggie.mapper.EmployeeMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TestEmp {

    @Autowired
    private EmployeeMapper employeeMapper;

    private static Long testEmpId;

    @Test
    @Order(1)
    void testInsert() {
        // 清理可能残留的测试数据
        LambdaQueryWrapper<Employee> cleanWrapper = new LambdaQueryWrapper<>();
        cleanWrapper.eq(Employee::getUsername, "test_crud");
        employeeMapper.delete(cleanWrapper);

        Employee e = new Employee();
        e.setName("测试员工");// 设置姓名
        e.setUsername("test_crud");// 设置用户名
        e.setPassword("123456");
        e.setPhone("13800001111");
        e.setSex("1");
        e.setIdNumber("110101200001010011");
        e.setStatus(1);
        e.setCreateTime(LocalDateTime.now());
        e.setUpdateTime(LocalDateTime.now());
        e.setCreateUser(1L);
        e.setUpdateUser(1L);

        int rows = employeeMapper.insert(e);
        assertEquals(1, rows);
        assertNotNull(e.getId(), "插入后应自动生成ID");
        testEmpId = e.getId();
        System.out.println("插入成功，ID = " + testEmpId);
    }

    @Test
    @Order(2)
    void testSelectById() {
        assertNotNull(testEmpId, "需要先执行插入测试");// 确认插入测试已经执行（NotNull）
        Employee e = employeeMapper.selectById(testEmpId);
        assertNotNull(e);
        assertEquals("test_crud", e.getUsername());
        assertEquals("测试员工", e.getName());
    }

    @Test
    @Order(3)
    void testSelectByUsername() {
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();// 构建查询条件
        wrapper.eq(Employee::getUsername, "test_crud");// 查询 username = "test_crud" 的员工
        Employee e = employeeMapper.selectOne(wrapper);// 执行查询
        assertNotNull(e);// 确认查询结果不为空
        assertEquals("测试员工", e.getName());// 确认查询结果的姓名正确
    }

    @Test
    @Order(4)
    void testSelectList() {
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Employee::getStatus, 1);
        List<Employee> list = employeeMapper.selectList(wrapper);
        assertFalse(list.isEmpty(), "应该至少有一条启用状态的员工");
        assertTrue(list.stream().anyMatch(emp -> "test_crud".equals(emp.getUsername())));
    }

    @Test
    @Order(5)
    void testUpdateById() {
        assertNotNull(testEmpId, "需要先执行插入测试");

        Employee e = new Employee();
        e.setId(testEmpId);
        e.setPhone("13900009999");
        e.setUpdateTime(LocalDateTime.now());
        e.setUpdateUser(1L);

        int rows = employeeMapper.updateById(e);
        assertEquals(1, rows);

        Employee updated = employeeMapper.selectById(testEmpId);
        assertEquals("13900009999", updated.getPhone());
    }

    @Test
    @Order(6)
    void testUpdateByWrapper() {
        assertNotNull(testEmpId, "需要先执行插入测试");

        LambdaUpdateWrapper<Employee> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Employee::getId, testEmpId)
               .set(Employee::getName, "测试员工-已修改")
               .set(Employee::getUpdateTime, LocalDateTime.now());

        int rows = employeeMapper.update(null, wrapper);
        assertEquals(1, rows);

        Employee updated = employeeMapper.selectById(testEmpId);
        assertEquals("测试员工-已修改", updated.getName());
    }

    @Test
    @Order(7)
    void testDeleteById() {
        assertNotNull(testEmpId, "需要先执行插入测试");

        int rows = employeeMapper.deleteById(testEmpId);
        assertEquals(1, rows);

        Employee e = employeeMapper.selectById(testEmpId);
        assertNull(e, "删除后查询应返回null");
    }
}
