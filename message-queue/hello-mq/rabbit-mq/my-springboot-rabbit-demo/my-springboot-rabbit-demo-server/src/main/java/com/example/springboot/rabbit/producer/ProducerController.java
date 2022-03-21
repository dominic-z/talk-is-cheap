package com.example.springboot.rabbit.producer;

import com.example.springboot.rabbit.config.ExchangeConfig;
import com.example.springboot.rabbit.config.QueueConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author dominiczhu
 * @version 1.0
 * @title ProducerController
 * @date 2022/1/19 11:02 上午
 */
@RestController
@RequestMapping("/producer")
@Slf4j
public class ProducerController {


    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping("sendConfirm/direct")
    public void sendConfirm(@RequestParam("id") String id, @RequestParam("message") String message,
                            @RequestParam("exchange") String exchange, @RequestParam("routingKey") String routingKey) {

        final String logMarker = "sendConfirm/direct";

        CorrelationData correlationData = new CorrelationData(id);

        final String targetExchange = StringUtils.isBlank(exchange) ? ExchangeConfig.NORMAL_DIRECT_EXCHANGE : exchange;
        final String targetRoutingKey = StringUtils.isBlank(routingKey) ? QueueConfig.NORMAL_Q_1_ROUTING : routingKey;
        log.info("{} targetExchange: {}, targetRoutingKey: {}, message: {}, correlationData: {}",
                logMarker, targetExchange, targetRoutingKey, message, correlationData);
        rabbitTemplate.convertAndSend(targetExchange, targetRoutingKey,
                message, correlationData);
        log.info("{} message {}已经被发送出去", logMarker, message);
    }


}
