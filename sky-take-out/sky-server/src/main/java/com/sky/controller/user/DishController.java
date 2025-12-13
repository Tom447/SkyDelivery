package com.sky.controller.user;


import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜品相关Controller
 */
@Slf4j
@Api(tags = "C菜品")
@RestController("UserDishController")
@RequestMapping("/user/dish")
public class DishController {

    @Autowired
    private DishService dishService;



    @GetMapping("/list")
    @ApiOperation("根据条件查询菜品")
    public Result getDishsByCondition(@RequestParam(required = false) Long categoryId)
    {
        log.info("条件查询获取菜品,条件：categoryId:{}",categoryId);
        List<Dish> dishList = dishService.getDishByCondition(categoryId);
        return Result.success(dishList);

    }
}
