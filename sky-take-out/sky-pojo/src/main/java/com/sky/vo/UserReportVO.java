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
public class UserReportVO implements Serializable {

    //日期，例如：2022-10-01,2022-10-02,2022-10-03
    private List dateList;

    //用户总量，例如：200,210,220
    private List totalUserList;

    //新增用户，例如：20,21,10
    private List newUserList;

}
