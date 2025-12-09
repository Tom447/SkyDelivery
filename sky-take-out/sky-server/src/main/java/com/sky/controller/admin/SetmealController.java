package com.sky.controller.admin;


import com.sky.context.BaseContext;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
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

    @ApiOperation("批量删除")
    @DeleteMapping()
    public Result delete(@RequestParam String ids){
        List<Long> list = Arrays.stream(ids.split(","))
                .map(String::trim).map(Long::parseLong).collect(Collectors.toList());
        setmealService.delete(list);
        return Result.success();
    }

    @ApiOperation("根据id查询套餐")
    @GetMapping("/{id}")
    public Result getSetmealById(@PathVariable Long id){
        log.info("查询id：{}的套餐",id);
        SetmealVO setmealVO = setmealService.getSetmealById(id);
        return Result.success(setmealVO);
    }

    @ApiOperation("修改套餐")
    @PutMapping
    public Result update(@RequestBody SetmealDTO setmealDTO){
        log.info("修改套餐:{}",setmealDTO);
        setmealService.update(setmealDTO);
        return Result.success();
    }

    @PutMapping("/status/{status}/{id}")
    @ApiOperation("起售/停售套餐")
    public Result updateStatus(@PathVariable Integer status, @PathVariable Long id) {
        log.info("修改套餐状态：id={}, status={}", id, status);
        setmealService.updateStatus(id, status);
        return Result.success();
    }

}
