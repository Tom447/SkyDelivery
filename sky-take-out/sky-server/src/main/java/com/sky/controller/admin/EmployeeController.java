package com.sky.controller.admin;




import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;


import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理Controller
 */
@Slf4j
@RestController
@RequestMapping("/admin/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;


    @Autowired
    JwtProperties jwtProperties;
    /**
     *
     * DTO:数据传输对象
     *
     * VO:View object 服务端返回给前端的对象
     *
     * Entity:与数据库表对应
     */
    @PostMapping("/login")
    public Result<EmployeeLoginVO> login(@RequestBody  EmployeeLoginDTO employeeLoginDTO){
        log.info("员工登录：{}",employeeLoginDTO);
        Employee employee = employeeService.login(employeeLoginDTO);
        //封装结果并返回，生成JWT令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String jwt = JwtUtil.createJWT(jwtProperties.getAdminSecretKey(), jwtProperties.getAdminTtl(), claims);


        //封装数据并返回
//        EmployeeLoginVO employeeLoginVO = new EmployeeLoginVO(employee.getId(), employee.getUsername(), employee.getName(), jwt);
        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .name(employee.getName())
                .userName(employee.getUsername())
                .token(jwt).build();
        return Result.success(employeeLoginVO);
    }

}
