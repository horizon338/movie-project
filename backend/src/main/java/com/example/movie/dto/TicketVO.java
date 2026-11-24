package com.example.movie.dto;

import java.time.LocalDateTime;
import java.util.List;

public record TicketVO(
        Long orderId,
        String orderNo,
        String movieTitle,
        String posterUrl,
        String cinemaName,
        String hallName,
        LocalDateTime startTime,
        List<OrderVO.SeatLabelVO> seats,
        String ticketCode,
        String qrBase64
) {
}
