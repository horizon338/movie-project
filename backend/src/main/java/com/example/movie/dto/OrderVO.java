package com.example.movie.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderVO(
        Long id,
        String orderNo,
        String status,
        BigDecimal totalAmount,
        LocalDateTime createdAt,
        LocalDateTime expireAt,
        LocalDateTime paidAt,
        LocalDateTime refundedAt,
        Long movieId,
        String movieTitle,
        String posterUrl,
        String cinemaName,
        String hallName,
        LocalDateTime startTime,
        String ticketCode,
        List<SeatLabelVO> seats
) {
    public record SeatLabelVO(Long id, int rowNo, int colNo, String type) {
    }
}
