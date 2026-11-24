package com.example.movie.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateOrderRequest(
        @NotNull(message = "场次不能为空") Long showtimeId,
        @NotEmpty(message = "请选择座位") List<Long> seatIds
) {
}
