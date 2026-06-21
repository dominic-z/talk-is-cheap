package org.talk.is.cheap.project.free.flow.starter.repository.config.properties;

import lombok.Data;

import java.time.Duration;


@Data
public class RedisProperties {
    private String host;
    private Integer port;
    private String username;
    private String password;
    private Integer database;
    private Integer timeout;

    private Pool pool;

    @Data
    public static class Pool{
        Integer maxActive;
        Integer maxIdle;
        Integer minIdle;
        Integer maxWait;
    }
}
