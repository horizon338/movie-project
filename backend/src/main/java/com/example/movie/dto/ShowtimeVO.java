package com.example.movie.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ShowtimeVO(
        Long id,
        LocalDateTime startTime,
        LocalDateTime endTime,
        BigDecimal price,
        String hallName,
        Long cinemaId,
        String cinemaName,
        int remainingSeats,
        int totalSeats
) {
}
