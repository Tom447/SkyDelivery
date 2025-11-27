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
public class SalesTop10ReportVO implements Serializable {

    //商品名称列表，例如：鱼香肉丝,宫保鸡丁,水煮鱼
    private List nameList;

    //销量列表，例如：260,215,200
    private List numberList;

}
