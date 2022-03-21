package com.zpc.rabbitmq.routing;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import com.zpc.rabbitmq.util.ConnectionUtil;

public class Send {

    private final static String EXCHANGE_NAME = "test_exchange_direct";

    public static void main(String[] argv) throws Exception {
        Thread consumer1 = new Thread(new Runnable() {
            public void run() {
                try {
                    Recv.consume();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Thread consumer2 = new Thread(new Runnable() {
            public void run() {
                try {
                    Recv2.consume();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Thread producer = new Thread(new Runnable() {
            public void run() {
                try {
                    send();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        consumer1.start();
        consumer2.start();
        Thread.sleep(20);
        producer.start();

        producer.join();
        consumer1.join();
        consumer2.join();
    }

    private static void send() throws Exception {
        // 获取到连接以及mq通道
        Connection connection = ConnectionUtil.getConnection();
        Channel channel = connection.createChannel();

        // 声明exchange
        channel.exchangeDeclare(EXCHANGE_NAME, "direct");

        // 消息内容
        String message = "Hello World! insert";
        channel.basicPublish(EXCHANGE_NAME, "insert", null, message.getBytes());
        System.out.println(" [x] Sent '" + message + "'");

        message = "Hello World! update";
        channel.basicPublish(EXCHANGE_NAME, "update", null, message.getBytes());
        System.out.println(" [x] Sent '" + message + "'");

        channel.close();
        connection.close();
    }
}