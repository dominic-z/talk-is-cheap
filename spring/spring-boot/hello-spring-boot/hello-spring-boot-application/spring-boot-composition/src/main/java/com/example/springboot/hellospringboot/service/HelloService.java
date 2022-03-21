package com.example.springboot.hellospringboot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author dominiczhu
 * @version 1.0
 * @title HelloService
 * @date 2021/9/14 下午8:36
 */
@Service
@Slf4j
public class HelloService {

    @Value("${hello-world}")
    private String hello;

    public void sayHello(){
        log.info(hello);
    }

}
