package com.example.movie.dto;

import com.example.movie.entity.Movie;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MovieListItemVO(
        Long id,
        String title,
        String posterUrl,
        String genre,
        Integer durationMinutes,
        BigDecimal scoreAvg,
        LocalDate releaseDate
) {
    public static MovieListItemVO from(Movie movie) {
        return new MovieListItemVO(
                movie.getId(),
                movie.getTitle(),
                movie.getPosterUrl(),
                movie.getGenre(),
                movie.getDurationMinutes(),
                movie.getScoreAvg(),
                movie.getReleaseDate()
        );
    }
}
