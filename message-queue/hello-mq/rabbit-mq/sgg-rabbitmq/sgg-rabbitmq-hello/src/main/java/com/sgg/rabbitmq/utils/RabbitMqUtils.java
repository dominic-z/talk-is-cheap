package com.sgg.rabbitmq.utils;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author dominiczhu
 * @version 1.0
 * @title RabbitMqUtils
 * @date 2022/1/17 11:23 下午
 */
public class RabbitMqUtils {

    private static Connection connection = null;

    public static Connection getConnection() throws IOException, TimeoutException {

        if (connection == null) {
            synchronized (RabbitMqUtils.class) {
                if (connection == null) {
                    ConnectionFactory factory = new ConnectionFactory();
                    factory.setHost("127.0.0.1");
                    factory.setUsername("admin");
                    factory.setPassword("admin");
                    factory.setPort(5672);
                    connection = factory.newConnection();
                }
            }
        }
        return connection;

    }

    //得到一个连接的 channel
    public static Channel getChannel() throws Exception {
        //创建一个连接工厂
        Channel channel = getConnection().createChannel();
        return channel;
    }
}
