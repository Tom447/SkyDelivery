package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TurnoverReportVO implements Serializable {

    //日期，例如：2022-10-01,2022-10-02,2022-10-03
    private List dateList;

    //营业额，例如：406.0,1520.0,75.0
    private List turnoverList;

}
