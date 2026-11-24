package com.example.movie.controller;

import com.example.movie.common.Result;
import com.example.movie.dto.CinemaVO;
import com.example.movie.entity.Cinema;
import com.example.movie.repository.CinemaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cinemas")
@RequiredArgsConstructor
public class CinemaController {

    private final CinemaRepository cinemaRepository;

    @GetMapping
    public Result<List<CinemaVO>> list() {
        List<CinemaVO> cinemas = cinemaRepository.findByStatusOrderByIdAsc(1)
                .stream().map(CinemaVO::from).toList();
        return Result.ok(cinemas);
    }
}
