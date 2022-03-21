package com.example.springboot.hellospringboot.domain.enums.redis;

/**
 * @author dominiczhu
 * @date 2022/1/16 11:06 上午
 */
public enum SemaphoreLimitType {

    /**
     * 自定义key
     */
    CUSTOMER,

    /**
     * 请求者IP
     */
    IP;
}
