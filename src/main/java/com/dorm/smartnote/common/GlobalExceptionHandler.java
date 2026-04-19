package com.dorm.smartnote.common;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        // 1. 在控制台打印真实错误日志（方便开发者自己看）
        System.err.println("系统运行异常: " + e.getMessage());
        e.printStackTrace();

        // 2. 返回给前端统一的友好提示，隐藏代码和SQL细节
        return Result.error("系统繁忙或数据冲突，请稍后再试");
    }
}