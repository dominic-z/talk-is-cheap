package com.sgg.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @author dominiczhu
 * @version 1.0
 * @title WarningConsumer
 * @date 2022/1/18 11:36 下午
 */
@Component
@Slf4j
public class WarningConsumer {
    public static final String WARNING_QUEUE_NAME = "warning.queue";

    @RabbitListener(queues = WARNING_QUEUE_NAME)
    public void receiveWarningMsg(Message message) {
        String msg = new String(message.getBody());
        log.error("队列: {} 报警发现不可路由消息：{}", WARNING_QUEUE_NAME, msg);
    }
}