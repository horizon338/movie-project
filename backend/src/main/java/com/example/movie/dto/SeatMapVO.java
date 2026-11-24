package com.example.movie.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record SeatMapVO(
        Long showtimeId,
        String movieTitle,
        String posterUrl,
        String cinemaName,
        String hallName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        BigDecimal price,
        int rows,
        int cols,
        List<SeatItemVO> seats
) {
    public record SeatItemVO(Long id, int rowNo, int colNo, String type, int status) {
    }
}
