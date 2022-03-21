package com.example.springboot.hellospringboot.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author dominiczhu
 * @version 1.0
 * @title MysqlProperties
 * @date 2021/9/14 下午8:13
 */
@ConfigurationProperties(prefix = "spring.datasource.redis")
@Data
public class RedisProperties {
    private String host;
    private int port;
    private String password;
}
