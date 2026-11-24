package com.example.movie.dto;

import com.example.movie.entity.Movie;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MovieDetailVO(
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
        BigDecimal scoreAvg
) {
    public static MovieDetailVO from(Movie movie) {
        return new MovieDetailVO(
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
                movie.getScoreAvg()
        );
    }
}
