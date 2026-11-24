package com.example.movie.controller;

import com.example.movie.common.Result;
import com.example.movie.dto.CinemaVO;
import com.example.movie.dto.admin.CinemaSaveRequest;
import com.example.movie.dto.admin.HallSaveRequest;
import com.example.movie.service.AdminCinemaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminCinemaController {

    private final AdminCinemaService adminCinemaService;

    @GetMapping("/cinemas")
    public Result<List<CinemaVO>> cinemas() {
        return Result.ok(adminCinemaService.listCinemas());
    }

    @PostMapping("/cinemas")
    public Result<CinemaVO> createCinema(@Valid @RequestBody CinemaSaveRequest request) {
        return Result.ok("影院已创建", adminCinemaService.createCinema(request));
    }

    @PutMapping("/cinemas/{id}")
    public Result<CinemaVO> updateCinema(@PathVariable Long id,
                                         @Valid @RequestBody CinemaSaveRequest request) {
        return Result.ok("影院已更新", adminCinemaService.updateCinema(id, request));
    }

    @GetMapping("/cinemas/{id}/halls")
    public Result<List<AdminCinemaService.HallVO>> halls(@PathVariable Long id) {
        return Result.ok(adminCinemaService.listHalls(id));
    }

    @PostMapping("/halls")
    public Result<AdminCinemaService.HallVO> createHall(@Valid @RequestBody HallSaveRequest request) {
        return Result.ok("影厅已创建", adminCinemaService.createHall(request));
    }
}
