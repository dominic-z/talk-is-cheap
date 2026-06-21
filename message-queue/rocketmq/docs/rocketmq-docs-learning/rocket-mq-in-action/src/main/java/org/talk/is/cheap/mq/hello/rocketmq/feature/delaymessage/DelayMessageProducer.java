package org.talk.is.cheap.mq.hello.rocketmq.feature.delaymessage;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientConfigurationBuilder;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.message.Message;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.apache.rocketmq.client.apis.producer.SendReceipt;

import java.io.IOException;

@Slf4j
// 可以先启动consumer
public class DelayMessageProducer {

    public static void main(String[] args) throws ClientException {
        // Endpoint address, set to the Proxy address and port list, usually xxx:8080;xxx:8081
        String endpoint = "localhost:8081";
        // The target Topic name for message sending, which needs to be created in advance.
        String topic = "DelayTopic";
        ClientServiceProvider provider = ClientServiceProvider.loadService();
        ClientConfigurationBuilder builder = ClientConfiguration.newBuilder().setEndpoints(endpoint);
        ClientConfiguration configuration = builder.build();
        // When initializing Producer, communication configuration and pre-bound Topic need to be set.
        Producer producer = provider.newProducerBuilder()
                .setTopics(topic)
                .setClientConfiguration(configuration)
                .build();
        //以下示例表示：延迟时间为10s之后的Unix时间戳。
        long deliverTimeStamp = System.currentTimeMillis() + 10L * 1000;
        Message message = provider.newMessageBuilder()
                .setTopic(topic)
                // Set the message index key, which can be used to accurately find a specific message.
                .setKeys("delayMessageKey")
                // Set the message Tag, used by the consumer to filter messages by specified Tag.
                .setTag("delayMessageTag")
                .setDeliveryTimestamp(deliverTimeStamp)
                // Message body.
                .setBody("delayMessageBody".getBytes())
                .build();
        try {
            // Send the message, paying attention to the sending result and catching exceptions.
            SendReceipt sendReceipt = producer.send(message);
            log.info("Send message successfully, messageId={}", sendReceipt.getMessageId());

            // 关闭
            producer.close();
        } catch (ClientException e) {
            log.error("Failed to send message", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
