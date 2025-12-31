package com.sky.service.impl;


import com.sky.dto.TurnoverReportDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrdersMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReportServiceImpl implements ReportService {


    @Autowired
    private OrdersMapper ordersMapper;

    private final DataSource dataSource;

    public ReportServiceImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        //1.获取日期列表
        List<String> dateList = getDateList(begin, end);
        dateList.stream().forEach(data -> {
            System.out.print(data.toString() + " ");
        });
        System.out.println();
        //2.统计制定日期范围的营业额  --sql
        //2.1查询数据库，获取原始结果
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<TurnoverReportDTO> turnoverReportDTOList = ordersMapper.selectTurnoverStatistics(beginTime, endTime, Orders.ORDER_STAUTS_COMPLETED);


        //2.2 原始效果 -->Map{key: 日期，value：营业额}
        Map<String, BigDecimal> dateMap = turnoverReportDTOList.stream().collect(Collectors.toMap(TurnoverReportDTO::getOrderDate, TurnoverReportDTO::getOrderMoney));

        //2.3遍历日期列表，获取每一个日期对应的营业额，如果没有营业额，设置为0

        List<BigDecimal> turnoverList = dateList.stream().map(date -> {
            return dateMap.get(date) == null ? new BigDecimal(0) : dateMap.get(date);
        }).collect(Collectors.toList());

        //3.封装数据并返回

        return new TurnoverReportVO(dateList, turnoverList);
    }

    /**
     * 获取指定范围内的日期
     *
     * @param begin
     * @param end
     * @return
     */
    private static List<String> getDateList(LocalDate begin, LocalDate end) {
        List<LocalDate> localDateList = begin.datesUntil(end.plusDays(1)).collect(Collectors.toList());

        List<String> dateList = localDateList.stream().map(localDate -> {
            return localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }).collect(Collectors.toList());
        return dateList;
    }
}
