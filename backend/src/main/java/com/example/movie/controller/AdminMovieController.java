package com.example.movie.controller;

import com.example.movie.common.PageResult;
import com.example.movie.common.Result;
import com.example.movie.dto.admin.AdminMovieVO;
import com.example.movie.dto.admin.MovieSaveRequest;
import com.example.movie.service.AdminMovieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/movies")
@RequiredArgsConstructor
public class AdminMovieController {

    private final AdminMovieService adminMovieService;

    @GetMapping
    public Result<PageResult<AdminMovieVO>> list(@RequestParam(required = false) String keyword,
                                                 @RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "10") int size) {
        return Result.ok(adminMovieService.list(keyword, page, size));
    }

    @PostMapping
    public Result<AdminMovieVO> create(@Valid @RequestBody MovieSaveRequest request) {
        return Result.ok("影片已创建", adminMovieService.create(request));
    }

    @PutMapping("/{id}")
    public Result<AdminMovieVO> update(@PathVariable Long id,
                                       @Valid @RequestBody MovieSaveRequest request) {
        return Result.ok("影片已更新", adminMovieService.update(id, request));
    }
}
