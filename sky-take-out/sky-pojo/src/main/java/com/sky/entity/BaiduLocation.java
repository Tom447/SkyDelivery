package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaiduLocation {
    private String address; // 或 addr
    private Double lat;     // 纬度
    private Double lng;     // 经度
    private String loation; //纬度,精度
}
