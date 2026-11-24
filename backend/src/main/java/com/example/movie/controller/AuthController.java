package com.example.movie.controller;

import com.example.movie.common.BusinessException;
import com.example.movie.common.Result;
import com.example.movie.dto.LoginRequest;
import com.example.movie.dto.LoginResponse;
import com.example.movie.dto.RegisterRequest;
import com.example.movie.dto.UserVO;
import com.example.movie.security.SecurityUtils;
import com.example.movie.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        return Result.ok("注册成功", authService.register(request));
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok("登录成功", authService.login(request));
    }

    @GetMapping("/me")
    public Result<UserVO> me() {
        Long userId = SecurityUtils.currentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        return Result.ok(authService.currentUser(userId));
    }
}
