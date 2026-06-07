package org.talk.is.cheap.mq.hello.rocketmq.feature.deadmsg;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.consumer.ConsumeResult;
import org.apache.rocketmq.client.apis.consumer.FilterExpression;
import org.apache.rocketmq.client.apis.consumer.FilterExpressionType;
import org.apache.rocketmq.client.apis.consumer.PushConsumer;
import org.apache.rocketmq.client.apis.consumer.SimpleConsumer;
import org.apache.rocketmq.client.apis.message.MessageView;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
public class BadMessageConsumer {

    public static void main(String[] args) throws ClientException, IOException, InterruptedException {
        final ClientServiceProvider provider = ClientServiceProvider.loadService();
        // Endpoint address, set to the Proxy address and port list, usually xxx:8080;xxx:8081
        String endpoints = "localhost:8081";
        ClientConfiguration clientConfiguration = ClientConfiguration.newBuilder()
                .setEndpoints(endpoints)
                .build();
        // Subscription message filtering rule, indicating subscription to all Tag messages.
        String tag = "*";
        FilterExpression filterExpression = new FilterExpression(tag, FilterExpressionType.TAG);
        // Specify the consumer group the consumer belongs to, Group needs to be created in advance.
        String consumerGroup = "BadConsumerGroup";
        // 2. 全新消费组，不要和原组重名
        String dlqConsumerGroup = "dlqBadConsumerGroup";
        // Specify which target Topic to subscribe to, Topic needs to be created in advance.
        String topic = "TestTopic";
        String topic2 = "TestTopic2";

        final int[] count = new int[1];
        // Initialize PushConsumer
        //消费示例一：使用PushConsumer消费普通消息，只需要在消费监听器中处理即可。
        PushConsumer pushConsumer = provider.newPushConsumerBuilder()
                .setClientConfiguration(clientConfiguration)
                // Set the consumer group.
                .setConsumerGroup(consumerGroup)
                // Set pre-bound subscription relationship.
                .setSubscriptionExpressions(Map.of(topic,filterExpression,topic2,filterExpression))
                // Set the message listener.
                .setMessageListener(messageView -> {
                    // Handle messages and return the consumption result.
                    log.info("Consume testTopic message {}, message={}, messageBody={}", count[0]++,
                            messageView, StandardCharsets.UTF_8.decode(messageView.getBody().duplicate()));
                    // 返回failure失败
                    return ConsumeResult.FAILURE;
                })
                .build();
        Thread.sleep(60000);
    }


}
