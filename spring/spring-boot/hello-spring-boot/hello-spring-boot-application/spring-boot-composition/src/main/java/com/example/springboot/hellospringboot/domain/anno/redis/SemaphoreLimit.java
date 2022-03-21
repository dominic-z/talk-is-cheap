package com.example.springboot.hellospringboot.domain.anno.redis;

import com.example.springboot.hellospringboot.domain.enums.redis.SemaphoreLimitType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author dominiczhu
 * @version 1.0
 * @title SemaphoreLimit
 * @date 2022/1/16 11:08 上午
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface SemaphoreLimit {
    /**
     * 名字
     */
    String name() default "";

    /**
     * key
     */
    String key() default "";

    /**
     * Key的前缀
     */
    String prefix() default "";

    /**
     * 给定的时间范围 单位(秒)
     */
    int period();

    /**
     * 一定时间内最多访问次数
     */
    int count();

    /**
     * 限流的类型(用户自定义key 或者 请求ip)
     */
    SemaphoreLimitType limitType() default SemaphoreLimitType.CUSTOMER;
}
