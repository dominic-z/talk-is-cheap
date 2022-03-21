package com.sgg.rabbitmq.two;

import com.rabbitmq.client.Channel;
import com.sgg.rabbitmq.utils.RabbitMqUtils;

import java.util.Scanner;

/**
 *
 * 抽取个util
 * @author dominiczhu
 * @version 1.0
 * @title Task01
 * @date 2022/1/17 11:39 下午
 */
public class Task01 {

    public static final String QUEUE_NAME = "hello";

    public static void main(String[] args) throws Exception {

        Channel channel = RabbitMqUtils.getChannel();

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            String message = scanner.next();
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
            System.out.println("消息发送完成：" + message);
        }

    }
}
