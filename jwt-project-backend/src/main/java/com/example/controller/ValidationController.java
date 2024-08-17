package com.example.controller;

import com.example.entity.RestBean;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ValidationController {
    @ExceptionHandler(ValidationException.class)
    public RestBean<Void> ValidateException(ValidationException exception){
        log.warn("Resolve[{}: {}]", exception.getClass().getName(),exception.getMessage());
        return RestBean.failure(400, "请求参数有误");
    }
}
