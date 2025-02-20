package com.example.Config;

import lombok.Data;

/**
 * 统一API响应结果封装
 */
@Data

public class ApiResponse {
    private String message;
    private Object data;

    // Constructors, getters, and setters
    // 构造函数
    public ApiResponse(String message, Object data) {
        this.message = message;
        this.data = data;
    }

    public static  ApiResponse success(Object data) {
        return new ApiResponse("操作成功", data);
    }
    public static  ApiResponse success() {
       return new ApiResponse("操作成功", null);
    }

    public static  ApiResponse error( String message) {
        return new ApiResponse(message, null);
    }
}