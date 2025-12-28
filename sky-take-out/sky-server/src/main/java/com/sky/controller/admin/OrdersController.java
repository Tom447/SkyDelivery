package com.sky.controller.admin;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import com.sky.service.OrdersService;
import com.sky.vo.OrderStatisticsVO;

import com.sky.vo.OrdersDetailVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公共controller
 */
@Slf4j
@Api(tags = "订单管理")
@RestController("adminOrdersController")
@RequestMapping("/admin/order")
public class OrdersController {


    @Autowired
    private OrdersService ordersService;

    @ApiOperation("动态条件分页查询订单的接口")
    @GetMapping("/conditionSearch")
    public Result<PageResult> page(OrdersPageQueryDTO ordersPageQueryDTO){
        log.info("通过info:{}动态条件分页查询订单", ordersPageQueryDTO);
        PageResult pageResult = ordersService.page(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    @ApiOperation("该接口用于统计各个状态的订单的数量")
    @GetMapping("/statistics")
    public Result<OrderStatisticsVO> statistics(){
        log.info("统计各个状态的订单的数量");
        OrderStatisticsVO orderStatisticsVO = ordersService.statistics();
        return Result.success(orderStatisticsVO);
    }

    @ApiOperation("查询订单详情")
    @GetMapping("/details/{id}")
    public Result<OrdersDetailVO> getOrdersDetail(@PathVariable Long id){
        log.info("根据订单id查询订单详情{}",id);
        OrdersDetailVO ordersDetailVO = ordersService.getOrdersDetailById(id);
        return Result.success(ordersDetailVO);
    }


}