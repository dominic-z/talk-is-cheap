package org.talk.is.cheap.mq.hello.rocketmq.feature.fifo;

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

@Slf4j
public class FIFOMessageConsumer {

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
        String consumerGroup = "FIFOConsumerGroup";
        // Specify which target Topic to subscribe to, Topic needs to be created in advance.
        String topic = "FIFOTopic";


        PushConsumer pushConsumer = provider.newPushConsumerBuilder()
                .setClientConfiguration(clientConfiguration)
                // Set the consumer group.
                .setConsumerGroup(consumerGroup)
                // Set pre-bound subscription relationship.
                .setSubscriptionExpressions(Collections.singletonMap(topic, filterExpression))
                // Set the message listener.
                .setMessageListener(messageView -> {
                    // Handle messages and return the consumption result.
                    // 可以看到数据是有序的，如果改为普通个队列，消费的顺序是乱序的，可以看草normalMessage的样例
                    log.info("Consume message successfully, message={}. data: {}", messageView,
                            StandardCharsets.UTF_8.decode(messageView.getBody().duplicate()));
                    return ConsumeResult.SUCCESS;
                })
                .build();
        Thread.sleep(Long.MAX_VALUE);
        // If PushConsumer is no longer needed, this instance can be closed.
        pushConsumer.close();
    }
}
