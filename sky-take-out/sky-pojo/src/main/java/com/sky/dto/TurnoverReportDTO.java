package com.sky.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TurnoverReportDTO implements Serializable {

    private String orderDate; //订单日期
    private BigDecimal orderMoney; //订单营业额

}
