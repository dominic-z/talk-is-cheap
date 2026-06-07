package org.talk.is.cheap.mq.hello.rocketmq.quickstart;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientConfigurationBuilder;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.message.Message;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.apache.rocketmq.client.apis.producer.SendReceipt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Slf4j
public class ProducerExample {
    private static final Logger logger = LoggerFactory.getLogger(ProducerExample.class);

    public static void main(String[] args) throws ClientException {
        // Endpoint address, set to the Proxy address and port list, usually xxx:8080;xxx:8081
        String endpoint = "localhost:8081";
        // The target Topic name for message sending, which needs to be created in advance.
        String topic = "TestTopic";
        ClientServiceProvider provider = ClientServiceProvider.loadService();
        ClientConfigurationBuilder builder = ClientConfiguration.newBuilder().setEndpoints(endpoint);
        ClientConfiguration configuration = builder.build();
        // When initializing Producer, communication configuration and pre-bound Topic need to be set.
        Producer producer = provider.newProducerBuilder()
                .setTopics(topic)
                .setClientConfiguration(configuration)
                .build();
        // Sending a normal message.
        Message message = provider.newMessageBuilder()
                .setTopic(topic)
                // Set the message index key, which can be used to accurately find a specific message.
                .setKeys("messageKey")
                // Set the message Tag, used by the consumer to filter messages by specified Tag.
                .setTag("messageTag")
                // Message body.
                .setBody("messageBody".getBytes())
                .build();
        try {
            // Send the message, paying attention to the sending result and catching exceptions.
            SendReceipt sendReceipt = producer.send(message);
            logger.info("Send message successfully, messageId={}", sendReceipt.getMessageId());

            // 关闭
            producer.close();
        } catch (ClientException e) {
            logger.error("Failed to send message", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}