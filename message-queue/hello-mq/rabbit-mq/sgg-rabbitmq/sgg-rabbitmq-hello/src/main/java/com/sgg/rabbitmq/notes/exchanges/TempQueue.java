package com.sgg.rabbitmq.notes.exchanges;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.sgg.rabbitmq.utils.RabbitMqUtils;

/**
 * @author dominiczhu
 * @version 1.0
 * @title TempQueue
 * @date 2022/1/18 3:04 下午
 */
public class TempQueue {

    public static void main(String[] args) throws Exception {
        final Channel channel = RabbitMqUtils.getChannel();

        final AMQP.Queue.DeclareOk declareOk = channel.queueDeclare();

        // 停止线程后，这个队列自动删除
    }
}
