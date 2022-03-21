package com.example.config;

import com.example.components.Apple;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * @author dominiczhu
 * @date 2020/8/12 10:34 上午
 */
@Configuration
public class ConfigurationDemo {
    @Bean
    @Scope("prototype")
    public Apple getApple(){
        Apple apple=new Apple();
        apple.setName("apple");
        return apple;
    }
}
