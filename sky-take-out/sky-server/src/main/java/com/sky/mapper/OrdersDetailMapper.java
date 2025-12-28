package com.sky.mapper;


import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrdersDetailMapper {

    /**
     * 批量保存订单明细
     * @param orderDetailList
     */
    void insertBatch(List<OrderDetail> orderDetailList);

    List<OrderDetail> getOrderDetailsByOrderIds(List<Long> orderIds);

    @Select("select * from order_detail where id = #{orderId}")
    List<OrderDetail> getByOrderId(Long orderId);
}


