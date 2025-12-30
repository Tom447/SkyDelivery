package com.sky.task;


import com.sky.entity.Orders;
import com.sky.mapper.OrdersMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class OrderTask {

    @Autowired
    private OrdersMapper ordersMapper;

    /**
     * 每30秒检查一次超时未支付订单（15分钟未支付自动取消）
     */
    @Scheduled(cron = "0/30 * * * * ?") // 更简单可靠
    public void cancelOrder() {
        log.info("【定时任务】开始检查超时未支付订单...");

        // ✅ 修正：15分钟前，不是15个月！
        LocalDateTime timeoutTime = LocalDateTime.now().minusMinutes(15);

        List<Orders> ordersList = ordersMapper.selectByStatusAndLtTime(
                Orders.ORDER_STAUTS_PENDING_PAYMENT,
                timeoutTime
        );

        if (!CollectionUtils.isEmpty(ordersList)) {
            ordersList.forEach(order -> {
                log.info("取消超时订单: id={}", order.getId());
                order.setStatus(Orders.ORDER_STAUTS_CANCELLED);
                order.setCancelTime(LocalDateTime.now());
                order.setCancelReason("支付超时，系统自动取消");
                ordersMapper.update(order);
            });
        } else {
            log.debug("无超时订单需要处理");
        }
    }

    /**
     * 每日凌晨1点完成订单
     */
    @Scheduled(cron = "0 0 1 * * ?") // 更简单可靠
    public void completeOrder() {
        //1.查询符号条件的（派送中， 2小时之前下单）的订单
        LocalDateTime timeoutTime = LocalDateTime.now().minusHours(2);

        List<Orders> ordersList = ordersMapper.selectByStatusAndLtTime(
                Orders.ORDER_STAUTS_DELIVERY_IN_PROGRESS,
                timeoutTime
        );
        //2.如果存在这样的订单，完成订单-修改订单的状态
        if (!CollectionUtils.isEmpty(ordersList)) {
            ordersList.forEach(order -> {
                log.info("执行定时完成订单的操作,{}", order.getId());
                order.setStatus(Orders.ORDER_STAUTS_CONFIRMED);
                order.setDeliveryTime(LocalDateTime.now());
                ordersMapper.update(order);
            });
        } else {
            log.debug("无超时订单需要处理");
        }
    }
}