package com.example.springboot.hellospringboot.config;

import com.example.springboot.hellospringboot.config.properties.RedisProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author dominiczhu
 * @version 1.0
 * @title MysqlDatasourceConfig
 * @date 2021/9/14 下午8:12
 */
@Configuration
@EnableConfigurationProperties(RedisProperties.class)
public class JedisConfig {

    @Autowired
    private RedisProperties redisProperties;

    @Bean
    public JedisPoolConfig jedisPoolConfig() {
        return new JedisPoolConfig();
    }

    @Bean
    public JedisPool jedisPool(@Autowired JedisPoolConfig jedisPoolConfig) {
        JedisPool jedisPool = new JedisPool(jedisPoolConfig, redisProperties.getHost(), redisProperties.getPort(), 2000,
                redisProperties.getPassword());
        return jedisPool;
    }


}
