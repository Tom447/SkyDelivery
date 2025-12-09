package com.sky.controller.admin;


import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 菜品相关Controller
 */
@Slf4j
@Api(tags = "菜品相关接口")
@RestController
@RequestMapping("/admin/dish")
public class DishController {

    @Autowired
    private DishService dishService;


    /**
     * 新增菜品数据
     * @param dishDTO
     * @return
     */
    @ApiOperation(value = "新增菜品数据")
    @PostMapping
    public Result save(@RequestBody DishDTO dishDTO){
        log.info("新增菜品数据:{}",dishDTO);
        dishService.save(dishDTO);
        return Result.success();
    }

    @ApiOperation("分页")
    @GetMapping("/page")
    public Result page(DishPageQueryDTO dishPageQueryDTO){
        log.info("条件分页查询:{}",dishPageQueryDTO);
        PageResult pageResult = dishService.page(dishPageQueryDTO);
        return Result.success(pageResult);
    }


}
