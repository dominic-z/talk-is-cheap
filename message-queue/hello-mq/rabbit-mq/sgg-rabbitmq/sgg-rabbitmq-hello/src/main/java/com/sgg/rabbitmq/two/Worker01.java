package com.sgg.rabbitmq.two;

import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.sgg.rabbitmq.utils.RabbitMqUtils;

/**
 *
 * 抽取个util
 * @author dominiczhu
 * @version 1.0
 * @title Worker01
 * @date 2022/1/17 11:26 下午
 */
public class Worker01 {

    private static final String QUEUE_NAME = "hello";

    public static void main(String[] args) throws Exception {

        Channel channel = RabbitMqUtils.getChannel();

        // 重启过docker，之前的队列没持久化，所以丢了，这里重新声明
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        //消息接受
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            System.out.println("worker1 deliverCallback consumerTag: " + consumerTag);
            String receivedMessage = new String(delivery.getBody());
            System.out.println("接收到消息:" + receivedMessage);
        };
        //消息被取消
        CancelCallback cancelCallback = (consumerTag) -> {
            System.out.println(consumerTag + "消费者取消消费接口回调逻辑");

        };

        System.out.println("C2 消费者启动等待消费.................. ");
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, cancelCallback);

    }
}
