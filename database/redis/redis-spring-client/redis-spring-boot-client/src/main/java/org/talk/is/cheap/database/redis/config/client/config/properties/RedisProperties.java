package org.talk.is.cheap.database.redis.config.client.config.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "spring.datasource.customize.redis")
@Data
public class RedisProperties {
    private String host;
    private Integer port;
    private String username;
    private String password;
    private Integer database;
    private Duration timeout;

    private Pool pool;

    @Data
    public static class Pool{
        Integer maxActive;
        Integer maxIdle;
        Integer minIdle;
        Duration maxWait;
    }



}
