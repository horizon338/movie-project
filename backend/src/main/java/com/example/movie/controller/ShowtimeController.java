package com.example.movie.controller;

import com.example.movie.common.Result;
import com.example.movie.dto.SeatMapVO;
import com.example.movie.service.ShowtimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/showtimes")
@RequiredArgsConstructor
public class ShowtimeController {

    private final ShowtimeService showtimeService;

    @GetMapping("/{id}/seats")
    public Result<SeatMapVO> seats(@PathVariable Long id) {
        return Result.ok(showtimeService.seatMap(id));
    }
}
