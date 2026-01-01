package com.sky.service.impl;


import com.sky.dto.OrderReportDTO;
import com.sky.dto.SalesTop10ReportDTO;
import com.sky.dto.TurnoverReportDTO;
import com.sky.dto.UserReportDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrdersMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.http.HttpResponse;
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

    @Override
    public OrderReportVO ordersStatistics(LocalDate begin, LocalDate end) {
        //1.获取日期列表
        List<String> dateList = getDateList(begin, end);
        //2.1获取每日的订单总数列表
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<OrderReportDTO> ordersByDayList = ordersMapper.countOrdersByOrderTimeAndStatus(beginTime, endTime, null);
        //2.2形成映射的map
        Map<String, Integer> ordersByDayMap = ordersByDayList.stream().collect(Collectors.toMap(OrderReportDTO::getOrderDate, OrderReportDTO::getOrderCount));
        //2.3得到需要的orderCountList
        List<Integer> orderCountList = dateList.stream().map(date -> {
            return ordersByDayMap.get(date) == null ? 0 : ordersByDayMap.get(date);
        }).collect(Collectors.toList());

        //3.获取每日的有效订单总数
        //3.1获取原始数据
        List<OrderReportDTO> validOrdersByDayList = ordersMapper.countOrdersByOrderTimeAndStatus(beginTime, endTime, Orders.ORDER_STAUTS_COMPLETED);
        //3.2得到原始数据的map
        Map<String, Integer> validOrdersByDayMap = validOrdersByDayList.stream().collect(Collectors.toMap(OrderReportDTO::getOrderDate, OrderReportDTO::getOrderCount));
        //3.3构造成需要的结果validOrderCountList
        List<Integer> validOrderCountList = dateList.stream().map(date -> {
            return validOrdersByDayMap.get(date) == null ? 0 : validOrdersByDayMap.get(date);
        }).collect(Collectors.toList());
        //4.获取订单的总数量
//        Integer totalOrderCount =  ordersByDayList.stream().mapToInt(OrderReportDTO::getOrderCount).sum();
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();
        //5.获取有效的订单数量
//        Integer validOrderCount =  validOrdersByDayList.stream().mapToInt(OrderReportDTO::getOrderCount).sum();
        Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();

        //6.订单的完成率
        Double orderCompletionRate = (totalOrderCount != 0)
                ? (double) validOrderCount / totalOrderCount
                : 0.0;
        return new OrderReportVO(dateList, orderCountList, validOrderCountList, totalOrderCount, validOrderCount, orderCompletionRate);
    }

    @Override
    public SalesTop10ReportVO top10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<SalesTop10ReportDTO> list = ordersMapper.top10(beginTime, endTime);

        List<String> nameList = new ArrayList<>();
        List<Integer> numberList = new ArrayList<>();
        list.stream().forEach(item -> {
            nameList.add(item.getSetmealOrDishName());
            numberList.add(item.getSetmealOrDishNumber());
        });

        return new SalesTop10ReportVO(nameList, numberList);
    }


    @Autowired
    private HttpServletResponse response;
    //导出统计数据
    @Override
    public void exportData() throws Exception {
        //1.近一个月的时间 - 开始，结束
        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now().minusDays(1);

        //2.调用mapper查询数据库中的原始数据 -- 数据概览

        BusinessDataVO businessDataVO = ordersMapper.countByTime(LocalDateTime.of(begin, LocalTime.MIN), LocalDateTime.of(end, LocalTime.MAX));
        Integer newUserCount = userMapper.countByTime(LocalDateTime.of(begin, LocalTime.MIN), LocalDateTime.of(end, LocalTime.MAX));

        String rangeDate = "时间范围： " + begin.toString() + " 到 " + end.toString();
        //3.1      加载报表Excel模版文件
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("templates/运营数据报表模板.xlsx");
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        //3.2      定位单元格，然后填充数据
        sheet.getRow(1).getCell(1).setCellValue(rangeDate);//2026年1月1日20:35:14
        sheet.getRow(3).getCell(2).setCellValue(businessDataVO.getTurnover());//营业额
        sheet.getRow(3).getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());//订单完成率
        sheet.getRow(3).getCell(6).setCellValue(newUserCount);//新增用户数
        sheet.getRow(4).getCell(2).setCellValue(businessDataVO.getValidOrderCount());//有效订单
        sheet.getRow(4).getCell(4).setCellValue(businessDataVO.getUnitPrice());//平均客单价


        //4.调用mapper查询数据库中的原始数据 -- 每一天的明细数据
        for (int i = 0; i < 30; i++){
            LocalDate everyDate = begin.plusDays(i);
            //  查询每一天的运营信息
            BusinessDataVO _businessDataVO = ordersMapper.countByTime(LocalDateTime.of(everyDate, LocalTime.MIN), LocalDateTime.of(everyDate, LocalTime.MAX));
            Integer _newUserCount = userMapper.countByTime(LocalDateTime.of(everyDate, LocalTime.MIN), LocalDateTime.of(everyDate, LocalTime.MAX));
        //5.填充明细数据

            //  填充数据
            Row row = sheet.getRow(7 + i);
            row.getCell(1).setCellValue(everyDate.toString());//日期
            row.getCell(2).setCellValue(_businessDataVO.getTurnover());//营业额
            row.getCell(3).setCellValue(_businessDataVO.getValidOrderCount());//有效订单数
            row.getCell(4).setCellValue(_businessDataVO.getOrderCompletionRate());//订单完成率
            row.getCell(5).setCellValue(_businessDataVO.getUnitPrice());//评价客单价
            row.getCell(6).setCellValue(_newUserCount);//新增用户数
        }

        //6.下载文件 -- 流
        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);//将Excel作为流响应给浏览器，浏览器接触到流数据会自动下载
        //7.释放资源
        workbook.close();
        outputStream.close();
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
