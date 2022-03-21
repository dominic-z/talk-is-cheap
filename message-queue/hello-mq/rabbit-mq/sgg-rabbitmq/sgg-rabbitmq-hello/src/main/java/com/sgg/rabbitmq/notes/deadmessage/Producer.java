package com.sgg.rabbitmq.notes.deadmessage;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.sgg.rabbitmq.utils.RabbitMqUtils;

/**
 * @author dominiczhu
 * @version 1.0
 * @title Producer
 * @date 2022/1/18 5:10 下午
 */
public class Producer {
    private static final String NORMAL_EXCHANGE = "normal_exchange";

    public static void main(String[] argv) throws Exception {
        Channel channel = RabbitMqUtils.getChannel();

        channel.exchangeDeclare(NORMAL_EXCHANGE, BuiltinExchangeType.DIRECT, false);
        //设置消息的 TTL 时间 10s
//        AMQP.BasicProperties properties = new AMQP.BasicProperties().builder().expiration("10000").build();
        AMQP.BasicProperties properties = null;
        //该信息是用作演示队列个数限制
        for (int i = 1; i < 11; i++) {
            String message = "info" + i;
            channel.basicPublish(NORMAL_EXCHANGE, "zhangsan", properties, message.getBytes());
            System.out.println("生产者发送消息:" + message);
        }

    }
}
