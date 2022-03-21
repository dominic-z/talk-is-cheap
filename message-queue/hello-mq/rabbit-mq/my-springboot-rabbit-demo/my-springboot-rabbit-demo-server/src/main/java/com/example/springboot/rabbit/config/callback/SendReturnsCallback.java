package com.example.springboot.rabbit.config.callback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * @author dominiczhu
 * @version 1.0
 * @title SendReturnsCallback
 * @date 2022/1/19 11:21 上午
 */
@Component
@Slf4j
public class SendReturnsCallback implements RabbitTemplate.ReturnsCallback {

    @Override
    public void returnedMessage(ReturnedMessage returnedMessage) {
        log.error("ReturnsCallback: {}", returnedMessage.toString());
    }
}
