package com.example.movie.dto.admin;

import com.example.movie.entity.Showtime;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminShowtimeVO(
        Long id,
        Long movieId,
        String movieTitle,
        Long cinemaId,
        String cinemaName,
        Long hallId,
        String hallName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        BigDecimal price,
        int totalSeats,
        int soldSeats
) {
    public static AdminShowtimeVO from(Showtime showtime, int soldSeats) {
        return new AdminShowtimeVO(
                showtime.getId(),
                showtime.getMovie().getId(),
                showtime.getMovie().getTitle(),
                showtime.getHall().getCinema().getId(),
                showtime.getHall().getCinema().getName(),
                showtime.getHall().getId(),
                showtime.getHall().getName(),
                showtime.getStartTime(),
                showtime.getEndTime(),
                showtime.getPrice(),
                showtime.getTotalSeats(),
                soldSeats
        );
    }
}
