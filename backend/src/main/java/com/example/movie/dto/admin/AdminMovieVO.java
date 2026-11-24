package com.example.movie.dto.admin;

import com.example.movie.entity.Movie;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record AdminMovieVO(
        Long id,
        String title,
        String posterUrl,
        String genre,
        Integer durationMinutes,
        String director,
        String actors,
        String synopsis,
        LocalDate releaseDate,
        LocalDate offShelfDate,
        BigDecimal scoreAvg,
        Integer status,
        LocalDateTime createdAt
) {
    public static AdminMovieVO from(Movie movie) {
        return new AdminMovieVO(
                movie.getId(),
                movie.getTitle(),
                movie.getPosterUrl(),
                movie.getGenre(),
                movie.getDurationMinutes(),
                movie.getDirector(),
                movie.getActors(),
                movie.getSynopsis(),
                movie.getReleaseDate(),
                movie.getOffShelfDate(),
                movie.getScoreAvg(),
                movie.getStatus(),
                movie.getCreatedAt()
        );
    }
}
