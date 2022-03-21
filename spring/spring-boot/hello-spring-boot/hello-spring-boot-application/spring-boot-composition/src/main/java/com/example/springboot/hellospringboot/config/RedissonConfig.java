package com.example.springboot.hellospringboot.config;

import com.example.springboot.hellospringboot.config.properties.RedisProperties;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author dominiczhu
 * @version 1.0
 * @title RedissonConfig
 * @date 2022/1/15 7:45 下午
 */
@Configuration
@EnableConfigurationProperties(RedisProperties.class)
@Slf4j
public class RedissonConfig {

    @Autowired
    private RedisProperties redisProperties;


    @Bean
    public RedissonClient redissonClient() {
        final Config redissonConfig = new Config();
//        should start with redis:// or rediss:// (for SSL connection)
        log.info(redisProperties.getHost());
        redissonConfig
                .setCodec(new StringCodec()) // 使用utf8编码，否则会往redis里存乱码
                .useSingleServer()
                .setAddress(String.format("redis://%s:%d", redisProperties.getHost(), redisProperties.getPort()));

        RedissonClient redissonClient = Redisson.create(redissonConfig);
        return redissonClient;
    }

}
