package com.example.movie.dto;

import com.example.movie.entity.User;

public record UserVO(
        Long id,
        String username,
        String nickname,
        String phone,
        String role
) {
    public static UserVO from(User user) {
        return new UserVO(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getPhone(),
                user.getRole().name()
        );
    }
}
