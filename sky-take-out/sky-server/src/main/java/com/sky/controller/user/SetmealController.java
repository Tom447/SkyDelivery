package com.sky.controller.user;


import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealDishVO;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
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


    @GetMapping("/list")
    @ApiOperation("根据分类id查询套餐")
    public Result<List<Setmeal>> getSetmealByCategoryId(@RequestParam(required = true) Long categoryId){
        log.info("根据分类id查询套餐，通过categoryid：{}查询套餐", categoryId);

        Setmeal setmeal = new Setmeal();
        setmeal.setCategoryId(categoryId);
        setmeal.setStatus(StatusConstant.ENABLE);

        List<Setmeal> res = setmealService.list(setmeal);
        return Result.success(res);
    }

    @ApiOperation("根据套餐ID查询包含的菜品列表")
    @GetMapping("/dish/{id}")
    public Result getSetmealById(@PathVariable Long id){
        log.info("查询id：{}查询包含的菜品",id);
        List<SetmealDishVO> setmealDishVOList = setmealService.getDishBySetmealId(id);
        return Result.success(setmealDishVOList);

    }

}
