package com.example.movie.controller;

import com.example.movie.common.PageResult;
import com.example.movie.common.Result;
import com.example.movie.dto.MovieDetailVO;
import com.example.movie.dto.MovieListItemVO;
import com.example.movie.dto.ShowtimeVO;
import com.example.movie.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @GetMapping
    public Result<PageResult<MovieListItemVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate releaseFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate releaseTo,
            @RequestParam(required = false) Long cinemaId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size) {
        return Result.ok(movieService.list(keyword, releaseFrom, releaseTo, cinemaId, page, size));
    }

    @GetMapping("/{id}")
    public Result<MovieDetailVO> detail(@PathVariable Long id) {
        return Result.ok(movieService.detail(id));
    }

    @GetMapping("/{id}/showtimes")
    public Result<List<ShowtimeVO>> showtimes(
            @PathVariable Long id,
            @RequestParam(required = false) Long cinemaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.ok(movieService.showtimes(id, cinemaId, date));
    }
}
