package com.sgg.rabbitmq.three;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.sgg.rabbitmq.utils.RabbitMqUtils;

import java.util.Scanner;

/**
 * 消息生产者,消息在手动应答时是不丢失的，放回队列重新消费
 *
 * @author zhiyuan
 */
public class Task02 {
    private static final String TASK_QUEUE_NAME = "ack_queue";

    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMqUtils.getChannel();


        //声明队列
        channel.queueDelete(TASK_QUEUE_NAME);
        boolean durable = false;
        channel.queueDeclare(TASK_QUEUE_NAME, durable, false, false, null);
        Scanner sc = new Scanner(System.in);
        System.out.println("请输入信息");
        while (sc.hasNext()) {
            String message = sc.nextLine();
            //发布消息
//            final AMQP.BasicProperties properties = MessageProperties.PERSISTENT_TEXT_PLAIN;
            final AMQP.BasicProperties properties = null;
            channel.basicPublish("", TASK_QUEUE_NAME, properties, message.getBytes("UTF-8"));
            System.out.println("生产者发出消息" + message);
        }
    }

}
