package com.sky.controller.user;


import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrdersService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrdersDetailVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Objects;

@Slf4j
@Api(tags = "C端-订单操作")
@RestController()
@RequestMapping("/user/order")
public class OrdersController {

    @Autowired
    private OrdersService ordersService;


    @ApiOperation("提交订单")
    @PostMapping("/submit")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) throws Exception {
        log.info("用户下单...");
        OrderSubmitVO orderSubmitVO = ordersService.submit(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }


    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("小程序中微信支付接口, 用于生成预支付交易单")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = ordersService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);
        if(Objects.isNull(orderPaymentVO)){
            return Result.error("支付失败");
        }else{
            return Result.success(orderPaymentVO);
        }
    }

    @GetMapping("/historyOrders")
    @ApiOperation("历史订单查询")
    public Result<PageResult> pageResultResult(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("历史订单查询:{}", ordersPageQueryDTO);
        PageResult pageResult = ordersService.page(ordersPageQueryDTO);
        return Result.success(pageResult);
    }


    /**
     * 根据ID查询订单详情
     *
     * @param id
     * @return
     */
    @ApiOperation("查询订单详情")
    @GetMapping("/orderDetail/{id}")
    public Result<OrdersDetailVO> getOrdersDetail(@PathVariable Long id) {
        log.info("根据订单id查询订单详情{}", id);
        OrdersDetailVO ordersDetailVO = ordersService.getOrdersDetailById(id);
        return Result.success(ordersDetailVO);
    }

    @ApiOperation("取消订单")
    @PutMapping("/cancel/{id}")
    public Result cancel(@PathVariable Long id) {
        log.info("取消id：{}订单", id);
        ordersService.cancel(id);
        return Result.success();
    }

    @ApiOperation("再来一单")
    @PostMapping("/repetition/{id}")
    public Result repetition(@PathVariable Long id) {
        log.info("订单Id:{}再来一单", id);
        ordersService.again(id);
        return Result.success();
    }
}