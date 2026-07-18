package com.wyc.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wyc.reggie.entity.Employee;
import com.wyc.reggie.mapper.EmployeeMapper;
import com.wyc.reggie.service.EmployeeService;
import org.springframework.stereotype.Service;
// 业务实现类
@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {
}
