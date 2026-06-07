package org.talk.is.cheap.mq.hello.rocketmq.feature.filter;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.consumer.ConsumeResult;
import org.apache.rocketmq.client.apis.consumer.FilterExpression;
import org.apache.rocketmq.client.apis.consumer.FilterExpressionType;
import org.apache.rocketmq.client.apis.consumer.PushConsumer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

@Slf4j
public class FilterMessageConsumer {

    public static void main(String[] args) throws ClientException, IOException, InterruptedException {
        final ClientServiceProvider provider = ClientServiceProvider.loadService();
        // Endpoint address, set to the Proxy address and port list, usually xxx:8080;xxx:8081
        String endpoints = "localhost:8081";
        ClientConfiguration clientConfiguration = ClientConfiguration.newBuilder()
                .setEndpoints(endpoints)
                .build();
        // Subscription message filtering rule, indicating subscription to all Tag messages.
        //只订阅消息标签为"TagA"、"TagB"或"TagC"的消息。
//        FilterExpression filterExpression = new FilterExpression("TagA||TagB||TagC", FilterExpressionType.TAG);
        //使用Tag标签过滤消息，订阅所有消息。
//        FilterExpression filterExpression = new FilterExpression("*", FilterExpressionType.TAG);

//        使用SQL过滤
        FilterExpression filterExpression = new FilterExpression(
                "Region IS NOT NULL AND Price IS NOT NULL AND Region = 'Hangzhou' AND Price > 5",
                FilterExpressionType.SQL92);
        //订阅所有消息。
//        FilterExpression filterExpression = new FilterExpression("True", FilterExpressionType.SQL92);

        // Specify the consumer group the consumer belongs to, Group needs to be created in advance.
        String consumerGroup = "YourConsumerGroup";
        // Specify which target Topic to subscribe to, Topic needs to be created in advance.
        String topic = "TestTopic";


        // Initialize PushConsumer
        //消费示例一：使用PushConsumer消费普通消息，只需要在消费监听器中处理即可。
        PushConsumer pushConsumer = provider.newPushConsumerBuilder()
                .setClientConfiguration(clientConfiguration)
                // Set the consumer group.
                .setConsumerGroup(consumerGroup)
                // Set pre-bound subscription relationship.
                .setSubscriptionExpressions(Collections.singletonMap(topic, filterExpression))
                // Set the message listener.
                .setMessageListener(messageView -> {
                    // Handle messages and return the consumption result.
                    log.info("Consume message successfully, message={}, messageBody={}",
                            messageView, StandardCharsets.UTF_8.decode(messageView.getBody().duplicate()).toString());
                    return ConsumeResult.SUCCESS;
                })
                .build();

        Thread.sleep(Long.MAX_VALUE);
        // If PushConsumer is no longer needed, this instance can be closed.
//         pushConsumer.close();


//        //消费示例二：使用SimpleConsumer消费普通消息，主动获取消息进行消费处理并提交消费结果。
//        SimpleConsumer simpleConsumer = provider.newSimpleConsumerBuilder().
//                setClientConfiguration(clientConfiguration)
//                .setConsumerGroup(consumerGroup)
//                .setSubscriptionExpressions(Collections.singletonMap(topic, filterExpression))
//                .setAwaitDuration(Duration.ofSeconds(60))
//                .build();
//        List<MessageView> messageViewList = null;
//        try {
//            messageViewList = simpleConsumer.receive(10, Duration.ofSeconds(30));
//            messageViewList.forEach(messageView -> {
//                System.out.println(messageView);
//                //消费处理完成后，需要主动调用ACK提交消费结果。
//                try {
//                    simpleConsumer.ack(messageView);
//                } catch (ClientException e) {
//                    e.printStackTrace();
//                }
//            });
//        } catch (ClientException e) {
//            //如果遇到系统流控等原因造成拉取失败，需要重新发起获取消息请求。
//            e.printStackTrace();
//        }
//        simpleConsumer.close();
    }
}
