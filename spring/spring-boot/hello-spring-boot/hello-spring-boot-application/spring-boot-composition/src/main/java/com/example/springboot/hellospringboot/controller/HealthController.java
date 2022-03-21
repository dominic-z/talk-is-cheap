package com.example.springboot.hellospringboot.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author dominiczhu
 * @version 1.0
 * @title HealthController
 * @date 2022/2/15 1:40 下午
 */
@RestController
@RequestMapping(path = "/web/health")
@Slf4j
public class HealthController {

    @ResponseBody
    @GetMapping(value = "/health")
    public String health() {
        return "UP";
    }
}
