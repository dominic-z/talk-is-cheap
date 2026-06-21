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

@Slf4j
public class DeadMessageConsumer {

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
        String deadTopic = "%DLQ%"+consumerGroup;



        // 消费死信队列
        SimpleConsumer simpleConsumer = provider.newSimpleConsumerBuilder().
                setClientConfiguration(clientConfiguration)
                .setConsumerGroup(dlqConsumerGroup)
                .setSubscriptionExpressions(Collections.singletonMap(deadTopic, filterExpression))
                .setAwaitDuration(Duration.ofSeconds(60))
                .build();
        try {
                while (true){

                    Thread.sleep(200);
                    List<MessageView> messageViewList = null;
                    messageViewList = simpleConsumer.receive(10, Duration.ofSeconds(30));
                    messageViewList.forEach(messageView -> {
                        log.info("消费死信{}, body: {}",messageView,
                                StandardCharsets.UTF_8.decode(messageView.getBody().duplicate()));
                        //消费处理完成后，需要主动调用ACK提交消费结果。
                        try {
                            // ⭐ 关键：不调用 ack()，而是调用 changeInvisibleTime 或直接忽略
                            // 超过 invisibleDuration 后 Broker 会自动重试
                            // 如果想立即触发重试（不等超时），可以显式设置极短的不可见时间：
                            simpleConsumer.ack(messageView);
                        } catch (ClientException e) {
                            e.printStackTrace();
                        }
                    });
                }
        } catch (ClientException e) {
            //如果遇到系统流控等原因造成拉取失败，需要重新发起获取消息请求。
            e.printStackTrace();
        }
    }


}
