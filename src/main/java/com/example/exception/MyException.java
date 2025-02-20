package com.example.exception;

/**
 * 业务异常类
 */
public class MyException extends RuntimeException {
    public MyException(String message) {
        super(message);
    }
}