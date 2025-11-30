package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.entity.Employee;
import com.sky.exception.BaseException;
import com.sky.exception.BusinessException;
import com.sky.exception.DataException;
import com.sky.mapper.EmployeeMapper;
import com.sky.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;


    /**
     *
     * @param employeeLoginDTO
     * @return
     */
    @Override
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {

        String password = employeeLoginDTO.getPassword();
        //1. 调用mapper，查询这个员工的信息
        Employee employee = employeeMapper.findByUsername(employeeLoginDTO.getUsername());
        //2. 判断这个员工是否存在，不存在，返回错误信息
        if (employee == null){
            log.info("查询到的员工信息为空，返回错误信息");
            throw new DataException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3. 校验密码的正确性，如果密码不正确，返回错误信息
        if (!password.equals(employee.getPassword())){
            log.info("查询的员工信息为空，返回错误信息");
            throw new BusinessException(MessageConstant.PASSWORD_ERROR);
        }
        //4.校验员工的状态，如果是禁用状态，则返回错误信息
        if (employee.getStatus() == StatusConstant.DISABLE){
            log.info("登录账号{}禁用, 禁止登录.", employeeLoginDTO.getUsername());
            throw new BusinessException(MessageConstant.ACCOUNT_LOCKED);
        }

        return employee;
    }
}
