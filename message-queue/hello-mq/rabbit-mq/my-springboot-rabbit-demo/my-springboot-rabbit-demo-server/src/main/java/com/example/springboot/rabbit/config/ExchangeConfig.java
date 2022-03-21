package com.example.springboot.rabbit.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * @author dominiczhu
 * @version 1.0
 * @title ExchangeConfig
 * @date 2022/1/19 10:48 上午
 */
@Configuration
@Slf4j
public class ExchangeConfig {

    public static final String NORMAL_DIRECT_EXCHANGE = "normal.direct.exchange";
    public static final String NORMAL_FANOUT_EXCHANGE = "normal.fanout.exchange";
    public static final String NORMAL_TOPIC_EXCHANGE = "normal.topic.exchange";
    public static final String DEAD_TOPIC_EXCHANGE = "dead.fanout.exchange";

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @Bean
    public DirectExchange normalDirectExchange() {
        Map<String, Object> args = new HashMap<>();

        return new DirectExchange(NORMAL_DIRECT_EXCHANGE, false, false, args);
    }

    @Bean
    public FanoutExchange normalFanoutExchange() {
        Map<String, Object> args = new HashMap<>();

        return new FanoutExchange(NORMAL_FANOUT_EXCHANGE, false, false, args);
    }

    @Bean
    public TopicExchange normalTopicExchange() {
        Map<String, Object> args = new HashMap<>();

        return new TopicExchange(NORMAL_TOPIC_EXCHANGE, false, false, args);
    }

    @Bean
    public FanoutExchange deadFanoutExchange() {
        Map<String, Object> args = new HashMap<>();

        return new FanoutExchange(DEAD_TOPIC_EXCHANGE, false, false, args);
    }
}
