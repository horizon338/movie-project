package com.example.movie.dto.admin;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record HallSaveRequest(
        @NotNull(message = "请选择影院") Long cinemaId,
        @NotBlank(message = "影厅名称不能为空") @Size(max = 50) String name,
        @NotNull @Min(1) @Max(30) Integer rowCount,
        @NotNull @Min(1) @Max(40) Integer colCount
) {
}
