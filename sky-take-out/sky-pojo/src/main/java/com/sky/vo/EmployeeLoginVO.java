package com.sky.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@Builder
@ApiModel("登录结果封装类")
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeLoginVO implements Serializable {


    @ApiModelProperty("ID")
    private Long id; //ID

    @ApiModelProperty("用户名")
    private String userName; // 用户名

    @ApiModelProperty("姓名")
    private String name; //姓名

    @ApiModelProperty("令牌")
    private String token; //令牌

}
