package com.wyc.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// 全局异常处理
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    // 针对自定义异常 AppException 的处理方法
    @ExceptionHandler(AppException.class)
    public R<?> handleAppException(AppException e) {
        return R.error(e.getMessage()); // 返回自定义的错误响应
    }
    @ExceptionHandler(Exception.class)
    public R<?> handleException(Exception e) {
        log.error("系统捕获到未处理异常：", e);
        return R.error("服务器异常"); // 返回通用的错误响应
    }

}
