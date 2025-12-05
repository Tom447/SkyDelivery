package com.sky.controller;


import com.sky.context.BaseContext;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 套餐管理controller
 */
@Slf4j
@Api(tags = "套餐管理")
@RestController
@RequestMapping("/admin/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @ApiOperation("新增套餐")
    @PostMapping
    public Result<PageResult> save(@RequestBody SetmealDTO setmealDTO){
        setmealService.save(setmealDTO);
        System.out.println(BaseContext.getCurrentId());
        return Result.success();
    }

    @ApiOperation("分页")
    @GetMapping("/page")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO){
        log.info("套餐分页查询:{}",setmealPageQueryDTO);
        PageResult pageResult = setmealService.page(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

}
