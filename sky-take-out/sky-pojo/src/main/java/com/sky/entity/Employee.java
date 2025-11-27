package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Employee implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    //用户名
    private String username;

    //姓名
    private String name;

    //密码
    private String password;

    //手机号
    private String phone;

    //性别 0 男 1 女
    private Integer sex;

    //身份证号
    private String idNumber;

    //状态 1 正常  0  禁用
    private Integer status;

    //创建时间
    private LocalDateTime createTime;

    //修改时间
    private LocalDateTime updateTime;

    //创建人
    private Long createUser;

    //修改人
    private Long updateUser;

}
