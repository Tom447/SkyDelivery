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


    private static final String LOGIN_PASSWORD_ERROR_KEY = "login:error";//密码错误标记的key
    private static final String LOGIN_LOCK_ERROR_KEY = "login:lock";//账号被锁定的key


    @Autowired
    private EmployeeMapper employeeMapper;
    @Autowired
    private RedisTemplate redisTemplate;




    /**
     *
     * @param employeeLoginDTO
     * @return
     */
    @Override
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        //前端传入的password
        String password = employeeLoginDTO.getPassword();
        String username = employeeLoginDTO.getUsername();
        //校验员工账号是否被锁定
        validateAccountLock(username);

        //1. 调用mapper，查询这个员工的信息
        Employee employee = employeeMapper.findByUsername(username);
        //2. 判断这个员工是否存在，不存在，返回错误信息
        if (employee == null){
            log.info("查询到的员工信息为空，返回错误信息");
            throw new DataException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3. 校验密码的正确性，如果密码不正确，返回错误信息
        String encodePassword = DigestUtils.md5DigestAsHex(password.getBytes());//对页面传递过来的明文加密
        if (!encodePassword.equals(employee.getPassword())){
            log.info("密码对比错误");
            //3.1 记录员工密码错误的标记，并设置有效期为5分钟
            redisTemplate.opsForValue().set(getKey(username), "-", 5, TimeUnit.MINUTES);
            //3.2 获取该员工的密码错误标记。如果这个标记的数量 >= 5, 设置账号锁定的标记
            Set keys = redisTemplate.keys(LOGIN_PASSWORD_ERROR_KEY + ":" + username + ":*");
            if (!keys.isEmpty() && keys.size() >= 5){
                log.info("员工登录在5分钟内输入的密码超过5次都是错误，锁定账号:"+username+" 1小时");
                redisTemplate.opsForValue().set(LOGIN_LOCK_ERROR_KEY + ":" + username, "-",1, TimeUnit.HOURS);
                throw new BusinessException(MessageConstant.LOGIN_LOCK_ERROR);
            }

            throw new BusinessException(MessageConstant.PASSWORD_ERROR);
        }
        //4.校验员工的状态，如果是禁用状态，则返回错误信息
        if (employee.getStatus() == StatusConstant.DISABLE){
            log.info("登录账号{}禁用, 禁止登录.", employeeLoginDTO.getUsername());
            throw new BusinessException(MessageConstant.ACCOUNT_LOCKED);
        }

        return employee;
    }

    private void validateAccountLock(String username) {
        Object flag = redisTemplate.opsForValue().get(LOGIN_LOCK_ERROR_KEY + ":"+ username);
        if (ObjectUtils.isNotEmpty(flag)){//账号被锁定
            log.info("被锁定，限制登录");
            throw new BusinessException(MessageConstant.LOGIN_LOCK_ERROR_MESSAGE);
        }
    }

    private static String getKey(String username) {
        return LOGIN_PASSWORD_ERROR_KEY + ":" + username + ":" + RandomStringUtils.randomAlphabetic(5);
    }
}
