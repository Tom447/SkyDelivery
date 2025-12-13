package com.sky.controller.admin;


import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.dto.SetmealDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
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

    @ApiOperation("批量删除")
    @DeleteMapping()
    public Result delete(@RequestParam String ids){
        List<Long> list = Arrays.stream(ids.split(","))
                .map(String::trim).map(Long::parseLong).collect(Collectors.toList());
        dishService.delete(list);
        return Result.success();
    }

    //根据id查询菜品
    @ApiOperation("根据id查询菜品")
    @GetMapping("/{id}")
    public Result getDishById(@PathVariable Long id){
        log.info("查询id：{}的菜品",id);
        DishVO dishVO = dishService.getDishById(id);
        return Result.success(dishVO);
    }

    @ApiOperation("修改菜品")
    @PutMapping
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("修改菜品:{}",dishDTO);
        dishService.update(dishDTO);
        return Result.success();
    }


    @PutMapping("/status/{status}/{id}")
    @ApiOperation("起售/停售菜品")
    public Result updateStatus(@PathVariable Integer status, @PathVariable Long id) {
        log.info("修改菜品状态：id={}, status={}", id, status);
        dishService.updateStatus(id, status);
        return Result.success();
    }
    @GetMapping("/list")
    @ApiOperation("根据条件查询菜品")
    public Result getDishsByCondition(@RequestParam(required = false) Long categoryId,
                                      @RequestParam(required = false) String name)
    {
        log.info("条件查询获取菜品,条件：categoryId:{}, name:{}",categoryId,name);
        List<Dish> dishList = dishService.getDishByCondition(categoryId, name);
        return Result.success(dishList);

    }
}
