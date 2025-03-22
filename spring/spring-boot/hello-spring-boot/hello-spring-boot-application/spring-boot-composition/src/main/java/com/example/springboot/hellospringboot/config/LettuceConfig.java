package com.example.springboot.hellospringboot.config;

import com.example.springboot.hellospringboot.config.properties.RedisProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.List;

/**
 * @author dominiczhu
 * @version 1.0
 * @title LettuceConfi
 * @date 2021/10/11 下午8:32
 */
@Configuration
@EnableConfigurationProperties(RedisProperties.class)
public class LettuceConfig {

    @Autowired
    private RedisProperties redisProperties;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // RedisStandaloneConfiguration这个配置类是Spring Data Redis2.0后才有的~~~
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        // 2.0后的写法
        configuration.setHostName(redisProperties.getHost());
        configuration.setPassword(RedisPassword.of(redisProperties.getPassword()));
        configuration.setPort(redisProperties.getPort());
        configuration.setDatabase(0);
        LettuceConnectionFactory factory = new LettuceConnectionFactory(configuration);
        // Spring Data Redis1.x这么来设置  2.0后建议使用RedisStandaloneConfiguration来取代
        //factory.setHostName("10.102.132.150");
        //factory.setPassword("123456");
        //factory.setPort(6379);
        //factory.setDatabase(0);
        return factory;
    }

    @Bean
    @Primary
    public StringRedisTemplate stringRedisTemplate(@Qualifier("redisConnectionFactory") RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate redisTemplate = new StringRedisTemplate();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        return redisTemplate;
    }





}
