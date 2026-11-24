package com.example.movie.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 20, message = "长度需为 3-20 位")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "仅支持字母、数字和下划线")
        String username,

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 32, message = "长度需为 6-32 位")
        String password,

        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "格式不正确")
        String phone,

        @Size(max = 20, message = "昵称最长 20 个字符")
        String nickname
) {
}
