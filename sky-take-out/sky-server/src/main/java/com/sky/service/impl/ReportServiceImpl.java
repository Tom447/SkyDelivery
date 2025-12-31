package com.sky.service.impl;


import com.sky.dto.TurnoverReportDTO;
import com.sky.dto.UserReportDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrdersMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReportServiceImpl implements ReportService {


    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private UserMapper userMapper;

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
     * 统计用户信息
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        //1. 获取日期列表
        List<String> dateList = getDateList(begin, end);

        //2. 获取指定时间范围内，用户新增的列表数据 --sql
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        //2.1   查询原始数据
        List<UserReportDTO> userReportVOList = userMapper.countAndByCreateTime(beginTime, endTime);
        //2.2   查询历史数据，将原始数据转为map{key:"日期"， value:"新增用户数量"}
        Map<String, Integer> dateMap = userReportVOList.stream().collect(Collectors.toMap(UserReportDTO::getCreateDate, UserReportDTO::getUserCount));

        //2.3   返回日期列表，从map集合中获取数据，如果没有，就设置为0
        List<Integer> newUserList = dateList.stream().map(date -> {
            return dateMap.get(date) == null ? 0 : dateMap.get(date);
        }).collect(Collectors.toList());


        //3. 获取指定时间范围内，用户总量列表的数据 totalUserList --sql
        //3.1 查询开始之前，一共多少用户 -- 基数baseCount
        Integer baseCount = userMapper.countTotalByCreateTime(beginTime);

        //3.2 累加->集合
        List<Integer> totalUserList = new ArrayList<>();
        for (Integer add : newUserList) {
            baseCount += add;
            totalUserList.add(baseCount);
        }

        return new UserReportVO(dateList, totalUserList, newUserList);
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
