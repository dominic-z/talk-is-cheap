package com.example.springboot.rabbit.config;

import com.example.springboot.rabbit.config.callback.SendConfirmCallback;
import com.example.springboot.rabbit.config.callback.SendReturnsCallback;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @author dominiczhu
 * @version 1.0
 * @title RabbitMQConfig
 * @date 2022/1/19 10:37 上午
 */
@Configuration
public class RabbitMQConfig {

    @Autowired
    private SendConfirmCallback sendConfirmCallback;

    @Autowired
    private SendReturnsCallback sendReturnsCallback;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Bean
    public RabbitAdmin rabbitAdmin(@Qualifier("rabbitConnectionFactory") ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @PostConstruct
    public void configureRabbitTemplate() {
        rabbitTemplate.setConfirmCallback(sendConfirmCallback);
        rabbitTemplate.setReturnsCallback(sendReturnsCallback);

    }

//    @Bean
//    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
//        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
//        //数据转换为json存入消息队列
//        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
//        return rabbitTemplate;
//    }
}
