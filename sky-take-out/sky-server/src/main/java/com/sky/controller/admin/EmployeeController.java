package com.sky.controller.admin;




import com.sky.constant.JwtClaimsConstant;
import com.sky.constant.PasswordConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理Controller
 */
@Slf4j
@Api(tags = "员工管理的接口")
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
    @ApiOperation("员工登录")
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


    /**
     * 新增员工
     */
    @ApiOperation("新增员工")
    @PostMapping
    public Result save(@RequestBody EmployeeDTO employeeDTO){
        log.info("新增员工");
        employeeService.save(employeeDTO);
        return Result.success();
    }


    /**
     * 分页查询
     * @param pageQueryDTO
     * @return
     */

    @ApiOperation("分页")
    @GetMapping("/page")
    public Result<PageResult> page(EmployeePageQueryDTO pageQueryDTO){
        log.info("条件分页查询，{}", pageQueryDTO);
        PageResult pageResult = employeeService.page(pageQueryDTO);
        return Result.success(pageResult);
    }


    /**
     * 启用/禁用员工
     */
    @ApiOperation("启用/禁用员工")
    @PutMapping("/status/{status}/{id}")
    public Result enableOrDisable(@PathVariable Integer status,@PathVariable Long id){
        log.info("启用/禁用员工,{},{}",status, id);
        employeeService.enableOrDisable(status, id);
        return Result.success();
    }

    /**
     * 根据id查员工
     */
    @ApiOperation("根据id查员工")
    @GetMapping("/{id}")
    public Result getEmployeeById(@PathVariable Long id){
        log.info("根据id：{}查员工",id);
        Employee employee = employeeService.getById(id);
        return Result.success(employee);
    }

    /**
     * 编辑员工信息
     */
    @ApiOperation("编辑员工信息")
    @PutMapping()
    public Result update(@RequestBody EmployeeDTO employeeDTO){
        log.info("编辑员工employee:{}信息", employeeDTO);
        employeeService.update(employeeDTO);
        return Result.success();
    }

    /**
     * 修改员工密码
     */
    @ApiOperation("修改员工密码")
    @PutMapping("/editPassword")
    public Result updatePassword(@RequestBody PasswordEditDTO editPasswordDTO){
        log.info("修改员工密码: {}",editPasswordDTO);
        employeeService.updatePassword(editPasswordDTO);
        return Result.success();
    }
}
