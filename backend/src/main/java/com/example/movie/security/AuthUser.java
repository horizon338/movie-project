package com.example.movie.security;

/**
 * 认证后写入 SecurityContext 的当前登录用户
 */
public record AuthUser(Long id, String username, String role) {
}
