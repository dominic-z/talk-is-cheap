package com.example.springboot.rabbit.consumer;

import com.example.springboot.rabbit.config.QueueConfig;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author dominiczhu
 * @version 1.0
 * @title NormalQueueConsumer
 * @date 2022/1/19 11:13 上午
 */
@Component
@Slf4j
public class NormalQueueConsumer {

    @RabbitListener(queues = QueueConfig.NORMAL_Q_1, ackMode = "MANUAL")
    public void consumeQ1(@Header(AmqpHeaders.RAW_MESSAGE) Message message,
                          @Header(AmqpHeaders.CHANNEL) Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) Long deliverTag) throws IOException {
        String msg = new String(message.getBody());
        log.info("从{} 消费消息: {}，deliverTag: {}", QueueConfig.NORMAL_Q_1, msg, deliverTag);

        if (StringUtils.equals(msg, "ack")) {
            channel.basicAck(deliverTag, false);
        } else {
            // 如果设置为true，这条信息会重新入队，然后再被重新消费，所以这段代码就会一直循环
            // 常见做法是有异常就送进其他队列
            channel.basicNack(deliverTag, false, true);
        }

    }
}
