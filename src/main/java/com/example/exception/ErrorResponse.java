package com.example.exception;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 错误响应DTO
 */
@Data
public class ErrorResponse {
    private String message;
    private LocalDateTime timestamp;

    public ErrorResponse(String message) {
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    // getters
}