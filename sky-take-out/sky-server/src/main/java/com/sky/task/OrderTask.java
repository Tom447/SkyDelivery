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
}