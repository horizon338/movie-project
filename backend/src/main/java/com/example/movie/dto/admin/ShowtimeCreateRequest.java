package com.example.movie.dto.admin;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ShowtimeCreateRequest(
        @NotNull(message = "请选择影片") Long movieId,
        @NotNull(message = "请选择影厅") Long hallId,
        @NotNull(message = "请选择开场时间") LocalDateTime startTime,
        @NotNull(message = "请填写票价") @DecimalMin(value = "0.01", message = "票价必须大于 0")
        BigDecimal price
) {
}
