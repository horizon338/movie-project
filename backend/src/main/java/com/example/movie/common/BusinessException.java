package com.example.movie.common;

import lombok.Getter;

/**
 * 业务异常，由全局异常处理器转换为统一响应
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        this(400, message);
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
