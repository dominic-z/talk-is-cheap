package com.sgg.rabbitmq.three;

import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.sgg.rabbitmq.utils.RabbitMqUtils;

/**
 *  轮询分发
 * 消费者01
 *
 * @author zhiyuan
 */
public class Work03 {
    private static final String TASK_QUEUE_NAME = "ack_queue";

    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMqUtils.getChannel();
        System.out.println("C1 等待接收消息处理时间较 短");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            System.out.println("C1 consumerTag: " + consumerTag);
            String message = new String(delivery.getBody());
            System.out.println("c1 接收到消息:" + message+" 开始sleep");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("接收到消息:" + message);
            /**
             * 1.消息标记 tag
             * 2.是否批量应答未应答消息
             */
            System.out.println("deliveryTag: "+delivery.getEnvelope().getDeliveryTag());
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        };

        CancelCallback cancelCallback = (s) -> {
            System.out.println(s + "消费者取消消费接口回调逻辑");
        };
        channel.basicQos(2);

        //采用手动应答
        boolean autoAck = false;
        channel.basicConsume(TASK_QUEUE_NAME, autoAck, deliverCallback, cancelCallback);


    }
}
