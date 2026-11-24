package com.example.movie.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SubmitReviewRequest(
        @NotNull(message = "影片不能为空") Long movieId,
        @NotNull(message = "请评分") @Min(value = 1, message = "评分为 1-10") @Max(value = 10, message = "评分为 1-10")
        Integer rating,
        @NotBlank(message = "评价内容不能为空") @Size(max = 500, message = "评价最多 500 字")
        String content
) {
}
