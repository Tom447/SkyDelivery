package com.sky.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserReportDTO implements Serializable {

    private String createDate; //注册日期 , 如: 2023-01-01
    private Integer userCount; //注册人数 , 如: 23

}
