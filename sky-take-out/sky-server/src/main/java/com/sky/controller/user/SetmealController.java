package com.sky.controller.user;


import com.sky.context.BaseContext;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 套餐管理controller
 */
@Slf4j
@Api(tags = "C端套餐管理")
@RestController("UserSetmealController")
@RequestMapping("/user/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

//    @GetMapping("/list")
//    @ApiOperation("根据条件查询菜品")
//    public Result getDishsByCondition(@RequestParam(required = true) Long categoryId)
//    {
//        log.info("条件查询获取菜品,条件：categoryId:{}",categoryId);
//
//        return Result.success(dishList);
//
//    }

}
