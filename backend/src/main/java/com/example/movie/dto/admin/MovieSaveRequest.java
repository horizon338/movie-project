package com.example.movie.dto.admin;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record MovieSaveRequest(
        @NotBlank(message = "片名不能为空") @Size(max = 100) String title,
        @Size(max = 500) String posterUrl,
        @NotNull(message = "片长不能为空") @Min(1) @Max(600) Integer durationMinutes,
        @Size(max = 100) String genre,
        @Size(max = 100) String director,
        @Size(max = 255) String actors,
        @Size(max = 2000) String synopsis,
        @NotNull(message = "上映日期不能为空") LocalDate releaseDate,
        LocalDate offShelfDate,
        @NotNull(message = "请选择状态") @Min(0) @Max(1) Integer status
) {
}
