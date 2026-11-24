package com.example.movie.controller;

import com.example.movie.common.Result;
import com.example.movie.dto.admin.BoxOfficeVO;
import com.example.movie.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
public class AdminStatsController {

    private final StatsService statsService;

    @GetMapping("/box-office")
    public Result<BoxOfficeVO> boxOffice(@RequestParam(defaultValue = "7") int days) {
        return Result.ok(statsService.boxOffice(days));
    }
}
