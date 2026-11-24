package com.example.movie.dto;

import com.example.movie.entity.Review;

import java.time.LocalDateTime;

public record ReviewVO(
        Long id,
        Long userId,
        String nickname,
        Integer rating,
        String content,
        LocalDateTime createdAt,
        boolean mine
) {
    public static ReviewVO from(Review review, Long currentUserId) {
        return new ReviewVO(
                review.getId(),
                review.getUser().getId(),
                review.getUser().getNickname(),
                review.getRating(),
                review.getContent(),
                review.getCreatedAt(),
                currentUserId != null && currentUserId.equals(review.getUser().getId())
        );
    }
}
