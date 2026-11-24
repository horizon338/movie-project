package com.example.movie.dto;

import com.example.movie.entity.Review;

import java.time.LocalDateTime;

public record MyReviewVO(
        Long id,
        Long movieId,
        String movieTitle,
        String posterUrl,
        Integer rating,
        String content,
        LocalDateTime createdAt
) {
    public static MyReviewVO from(Review review) {
        return new MyReviewVO(
                review.getId(),
                review.getMovie().getId(),
                review.getMovie().getTitle(),
                review.getMovie().getPosterUrl(),
                review.getRating(),
                review.getContent(),
                review.getCreatedAt()
        );
    }
}
