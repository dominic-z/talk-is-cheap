package com.example.springboot.hellospringboot.service;

import com.example.springboot.hellospringboot.domain.anno.redis.SemaphoreLimit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author dominiczhu
 * @version 1.0
 * @title RedisLimitService
 * @date 2022/1/16 11:19 上午
 */
@Service
@Slf4j
public class RedisAopLimitService {


    @SemaphoreLimit(period = 10, count = 3)
    public String callWithLimit(int id) {
        final String format = String.format("id %s limit pass", id);
        log.info(format);
        return format;
    }

}
