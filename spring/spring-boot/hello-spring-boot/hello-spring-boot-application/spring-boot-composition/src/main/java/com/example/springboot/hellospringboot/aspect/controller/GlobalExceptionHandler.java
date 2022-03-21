package com.example.springboot.hellospringboot.aspect.controller;

import com.example.springboot.hellospringboot.domain.messages.ValidateResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;

/**
 * @author dominiczhu
 * @version 1.0
 * @title GlobalExceptionHandler
 * @date 2022/1/25 11:20 上午
 */
@ControllerAdvice
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 请求参数异常处理
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolationException(ConstraintViolationException ex,
                                                                HttpServletRequest request) {
        log.info("进入异常处理器， request:{} , ex: {}", request, ex);
        ValidateResp resp = new ValidateResp();
        resp.setCode(200);
        final ResponseEntity<ValidateResp> responseEntity = new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        return responseEntity;

    }
}
