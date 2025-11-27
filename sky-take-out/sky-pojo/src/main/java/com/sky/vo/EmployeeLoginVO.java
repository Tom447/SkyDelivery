package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeLoginVO implements Serializable {

    private Long id; //ID

    private String userName; // 用户名

    private String name; //姓名

    private String token; //令牌

}
