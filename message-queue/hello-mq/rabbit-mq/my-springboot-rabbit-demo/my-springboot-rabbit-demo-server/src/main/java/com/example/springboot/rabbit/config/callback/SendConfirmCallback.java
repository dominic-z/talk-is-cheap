package com.example.springboot.rabbit.config.callback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * @author dominiczhu
 * @version 1.0
 * @title SendConfirmCallback
 * @date 2022/1/19 11:16 上午
 */
@Component
@Slf4j
public class SendConfirmCallback implements RabbitTemplate.ConfirmCallback {
    /**
     * 交换机不管是否收到消息的一个回调方法
     *
     * @param correlationData 消息相关数据
     * @param ack             交换机是否收到消息
     * @param cause           为收到消息的原因
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        log.info("ConfirmCallback: 确认状态 ack: {} correlationData 为: {}的消息 cause:{}，returnedMessage：{}",
                ack, correlationData, cause, correlationData.getReturned());
    }
}
