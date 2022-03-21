package com.sgg.rabbitmq.priority;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.sgg.rabbitmq.utils.RabbitMqUtils;

/**
 * @author dominiczhu
 * @version 1.0
 * @title PriorityProducer
 * @date 2022/1/19 9:43 上午
 */
public class PriorityProducer {
    private static final String QUEUE_NAME = "hello";

    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMqUtils.getChannel();

        //给消息赋予一个 priority 属性

        for (int i = 1; i < 11; i++) {
            String message = "info" + i;
            if (i > 5) {
                AMQP.BasicProperties properties = new AMQP.BasicProperties().builder().priority(i).build();
                channel.basicPublish("", QUEUE_NAME, properties, message.getBytes());
            } else {
                channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
            }
            System.out.println("发送消息完成:" + message);
        }
    }

}

