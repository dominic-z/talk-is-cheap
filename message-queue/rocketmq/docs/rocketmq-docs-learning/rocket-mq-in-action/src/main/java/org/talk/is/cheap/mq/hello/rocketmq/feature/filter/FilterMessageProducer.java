package org.talk.is.cheap.mq.hello.rocketmq.feature.filter;


import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientConfigurationBuilder;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.message.Message;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.apache.rocketmq.client.apis.producer.SendReceipt;

import java.io.IOException;
import java.util.Random;

@Slf4j
public class FilterMessageProducer {


    public static void main(String[] args) throws ClientException {
        // Endpoint address, set to the Proxy address and port list, usually xxx:8080;xxx:8081
        String endpoint = "localhost:8081";
        // The target Topic name for message sending, which needs to be created in advance.
        String topic = "TestTopic"; // 复用这个topic
        ClientServiceProvider provider = ClientServiceProvider.loadService();
        ClientConfigurationBuilder builder = ClientConfiguration.newBuilder().setEndpoints(endpoint);
        ClientConfiguration configuration = builder.build();
        // When initializing Producer, communication configuration and pre-bound Topic need to be set.
        Producer producer = provider.newProducerBuilder()
                .setTopics(topic)
                .setClientConfiguration(configuration)
                .build();

        try {
            // 给个按照Tag过滤的消息
            for (int i = 0; i < 10; i++) {
                // Sending a normal message.
                String tag = "Tag"+(char)('A'+i%5);
                Message message = provider.newMessageBuilder()
                        .setTopic(topic)
                        // Set the message index key, which can be used to accurately find a specific message.
                        .setKeys("messageKey")
                        // Set the message Tag, used by the consumer to filter messages by specified Tag.
                        .setTag(tag)
                        // Message body.
                        .setBody(("messageBody"+i).getBytes())
                        .build();
                // Send the message, paying attention to the sending result and catching exceptions.
                SendReceipt sendReceipt = producer.send(message);
                log.info("Send message successfully, messageId={}", sendReceipt.getMessageId());
            }


            Random random = new Random();
            // 给个按照Property过滤的消息
            for (int i = 0; i < 10; i++) {
                // Sending a normal message.
                String tag = "Tag"+(char)('A'+i%5);
                Message message = provider.newMessageBuilder()
                        .setTopic(topic)
                        // Set the message index key, which can be used to accurately find a specific message.
                        .setKeys("messageKey")
                        // Set the message Tag, used by the consumer to filter messages by specified Tag.
                        //消息也可以设置自定义的分类属性，例如环境标签、地域、逻辑分支。
                        //该示例表示为消息自定义一个属性，该属性为地域，属性值为杭州。
                        .addProperty("Region", i%2==0?"Hangzhou":"Shenzhen")
                        .addProperty("Price", Integer.toString(i))
                        // Message body.
                        .setBody(("messageBody"+i).getBytes())
                        .build();
                // Send the message, paying attention to the sending result and catching exceptions.
                SendReceipt sendReceipt = producer.send(message);
                log.info("Send message successfully, messageId={}", sendReceipt.getMessageId());
            }


            // 关闭
            producer.close();
        } catch (ClientException e) {
            log.error("Failed to send message", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
