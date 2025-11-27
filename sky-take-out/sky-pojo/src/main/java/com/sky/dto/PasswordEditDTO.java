package com.sky.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordEditDTO implements Serializable {

    //员工id
    private Long empId;

    //旧密码
    private String oldPassword;

    //新密码
    private String newPassword;

}
