package com.sky.mapper;


import com.sky.dto.OrderReportDTO;
import com.sky.dto.TurnoverReportDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrdersMapper {



    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void insert(Orders orders);

    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    void update(Orders orders);

    List<Orders> list(Orders orders);

    @Select("select * from orders where id = #{orderId}")
    Orders getById(Long orderId);
    @Select("select * from orders where status = #{status} and order_time < #{before15Time}")
    List<Orders> selectByStatusAndLtTime(int status, LocalDateTime before15Time);

    List<TurnoverReportDTO> selectTurnoverStatistics(LocalDateTime beginTime, LocalDateTime endTime, Integer status);

    List<OrderReportDTO> countOrdersByOrderTimeAndStatus(LocalDateTime beginTime, LocalDateTime endTime, Integer status);
}

