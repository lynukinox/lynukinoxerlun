package com.dorm.smartnote.model.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class UserLoginDTO {
    @NotBlank(message = "用户名或邮箱不能为空")
    private String account; // 支持用户名或邮箱登录

    @NotBlank(message = "密码不能为空")
    private String password;
}