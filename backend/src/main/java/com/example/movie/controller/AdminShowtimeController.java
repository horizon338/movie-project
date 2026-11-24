package com.example.movie.controller;

import com.example.movie.common.Result;
import com.example.movie.dto.admin.AdminShowtimeVO;
import com.example.movie.dto.admin.ShowtimeCreateRequest;
import com.example.movie.service.AdminShowtimeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/showtimes")
@RequiredArgsConstructor
public class AdminShowtimeController {

    private final AdminShowtimeService adminShowtimeService;

    @GetMapping
    public Result<List<AdminShowtimeVO>> list(
            @RequestParam(required = false) Long movieId,
            @RequestParam(required = false) Long cinemaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.ok(adminShowtimeService.list(movieId, cinemaId, date));
    }

    @PostMapping
    public Result<AdminShowtimeVO> create(@Valid @RequestBody ShowtimeCreateRequest request) {
        return Result.ok("排片成功", adminShowtimeService.create(request));
    }
}
