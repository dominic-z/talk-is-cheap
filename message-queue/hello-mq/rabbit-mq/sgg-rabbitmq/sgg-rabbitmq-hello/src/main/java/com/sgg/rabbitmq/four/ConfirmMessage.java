package com.sgg.rabbitmq.four;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmCallback;
import com.sgg.rabbitmq.utils.RabbitMqUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * @author dominiczhu
 * @version 1.0
 * @title ConfirmMessage
 * @date 2022/1/18 1:30 下午
 */
public class ConfirmMessage {

    private static final int MESSAGE_COUNT = 100;


    public static void main(String[] args) throws Exception {
//        publishMessageIndividually();
//        publishMessageBatch();
        publishMessageAsync();
    }

    /**
     * 单个发送
     */
    public static void publishMessageIndividually() throws Exception {
        Channel channel = RabbitMqUtils.getChannel();
        //队列声明
        String queueName = UUID.randomUUID().toString();
        channel.queueDeclare(queueName, false, false, false, null);
        //开启发布确认
        channel.confirmSelect();

        long begin = System.currentTimeMillis();

        for (int i = 0; i < MESSAGE_COUNT; i++) {
            String message = i + "";
            channel.basicPublish("", queueName, null, message.getBytes());
            //服务端返回 false 或超时时间内未返回，生产者可以消息重发
            boolean flag = channel.waitForConfirms();
            System.out.println("is confirm " + flag);
            if (flag) {
                System.out.println("消息发送成功");
            }
        }

        long end = System.currentTimeMillis();
        System.out.println("发布" + MESSAGE_COUNT + "个单独确认消息,耗时" + (end - begin) + "ms");

    }


    /**
     * 批量
     */
    public static void publishMessageBatch() throws Exception {
        Channel channel = RabbitMqUtils.getChannel();
        //队列声明
        String queueName = UUID.randomUUID().toString();
        channel.queueDeclare(queueName, false, false, false, null);
        //开启发布确认
        channel.confirmSelect();
        //批量确认消息大小
        int batchSize = 100;
        //未确认消息个数
        int outstandingMessageCount = 0;
        long begin = System.currentTimeMillis();

        for (int i = 0; i < MESSAGE_COUNT; i++) {
            String message = i + "";
            channel.basicPublish("", queueName, null, message.getBytes());
            outstandingMessageCount++;
            if (outstandingMessageCount == batchSize) {
                final boolean confirms = channel.waitForConfirms();
                System.out.println("is confirm " + confirms);
                outstandingMessageCount = 0;
            }
        }
        //为了确保还有剩余没有确认消息 再次确认
        if (outstandingMessageCount > 0) {
            channel.waitForConfirms();
        }
        long end = System.currentTimeMillis();
        System.out.println("发布" + MESSAGE_COUNT + "个批量确认消息,耗时" + (end - begin) + "ms");
    }


    public static void publishMessageAsync() throws Exception {
        Channel channel = RabbitMqUtils.getChannel();

        //队列声明
        String queueName = "async-confirm-queue";
        channel.queueDelete(queueName);
        channel.queueDeclare(queueName, false, false, false, null);
        //开启发布确认
        channel.confirmSelect();

        /**
         * 线程安全有序的哈希表，适用于高并发
         * 1. 将信号与消息进行关联
         * 2. 轻松批量删除条目
         */
        ConcurrentHashMap<Long, String> outstandingConfirms = new ConcurrentHashMap<>();

        //未确认消息个数
        int outstandingMessageCount = 0;
        long begin = System.currentTimeMillis();

        Deque<Long> deliveryTags = new LinkedList<>();

        // 准备消息的监听器 监听成功消息与失败消息
        ConfirmCallback ackConfirmCallback = (deliveryTag, multiple) -> {
            String logMessage = "multiple: " + multiple +
                    " 已确认消息-deliveryTag:" + deliveryTag;
            if (multiple) {
                while (!deliveryTags.isEmpty() && deliveryTags.getFirst() <= deliveryTag) {
                    final Long confirmedTag = deliveryTags.removeFirst();
                    System.out.println(logMessage + " confirmTag: " + confirmedTag +
                            " message: " + outstandingConfirms.get(confirmedTag));
                    outstandingConfirms.remove(confirmedTag);
                }
            } else {
                logMessage = logMessage + " message: " + outstandingConfirms.get(deliveryTag);
                System.out.println(logMessage);
                outstandingConfirms.remove(deliveryTag);
            }

        };

        ConfirmCallback nackConfirmCallback = (deliveryTag, multiple) -> {
            System.out.println("未确认消息:" + deliveryTag);
        };

        // 异步监听
        channel.addConfirmListener(ackConfirmCallback, nackConfirmCallback);

        for (int i = 0; i < MESSAGE_COUNT; i++) {
            String message = "发布消息" + i;
            final long nextPublishSeqNo = channel.getNextPublishSeqNo();
            deliveryTags.add(nextPublishSeqNo);
            outstandingConfirms.put(nextPublishSeqNo, message);
            channel.basicPublish("", queueName, null, message.getBytes(StandardCharsets.UTF_8));
            // 1. 记录所有要发送的消息
        }

        long end = System.currentTimeMillis();
        System.out.println("发布" + MESSAGE_COUNT + "个异步确认消息,耗时" + (end - begin) + "ms");
        System.out.println("等待剩余消息");

        Thread.sleep(3000);
        System.out.println("剩余的消息包含" + outstandingConfirms);
    }

}
