package com.example.springboot.rabbit.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dominiczhu
 * @version 1.0
 * @title QueueConfig
 * @date 2022/1/19 10:26 上午
 */
@Configuration
public class QueueConfig {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    public static final String NORMAL_Q_1 = "normal.queue.1";
    public static final String NORMAL_Q_2 = "normal.queue.2";
    public static final String NORMAL_Q_3 = "normal.queue.3";
    public static final String NORMAL_Q_4 = "normal.queue.4";

    public static final String NORMAL_Q_1_ROUTING = "normal.queue.1";
    public static final String NORMAL_Q_2_ROUTING = "normal.queue.2";
    public static final String NORMAL_Q_3_ROUTING = "normal.queue.3.a#";
    public static final String NORMAL_Q_4_ROUTING = "normal.queue.4.b*";

    public static final String DEAD_Q_1 = "dead.queue.1";

    @Bean
    public Queue normalQ1() {
        Map<String, Object> args = new HashMap<>();

        rabbitAdmin.deleteQueue(NORMAL_Q_1);
        QueueBuilder builder = QueueBuilder.nonDurable(NORMAL_Q_1).withArguments(args);
        return builder.build();
    }

    @Bean
    public Binding normalQ1DirectBinding(@Qualifier("normalQ1") Queue queue,
                                         @Qualifier("normalDirectExchange") DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(NORMAL_Q_1_ROUTING);
    }

    @Bean
    public Queue normalQ2() {
        Map<String, Object> args = new HashMap<>();

        rabbitAdmin.deleteQueue(NORMAL_Q_2);
        QueueBuilder builder = QueueBuilder.nonDurable(NORMAL_Q_2).withArguments(args);
        return builder.build();
    }

    @Bean
    public Binding normalQ2FanoutBinding(@Qualifier("normalQ2") Queue queue,
                                         @Qualifier("normalFanoutExchange") FanoutExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange);
    }

    @Bean
    public Queue normalQ3() {
        Map<String, Object> args = new HashMap<>();
        rabbitAdmin.deleteQueue(NORMAL_Q_3);

        QueueBuilder builder = QueueBuilder.nonDurable(NORMAL_Q_3).withArguments(args);
        return builder.build();
    }

    @Bean
    public Binding normalQ3TopicBinding(@Qualifier("normalQ3") Queue queue,
                                        @Qualifier("normalTopicExchange") TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(NORMAL_Q_3_ROUTING);
    }

    @Bean
    public Queue normalQ4() {
        Map<String, Object> args = new HashMap<>();

        QueueBuilder builder = QueueBuilder.nonDurable(NORMAL_Q_4).withArguments(args);
        return builder.build();
    }

    @Bean
    public Binding normalQ4TopicBinding(@Qualifier("normalQ4") Queue queue,
                                        @Qualifier("normalTopicExchange") TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(NORMAL_Q_4_ROUTING);
    }


    @Bean
    public Queue deadQ1() {
        Map<String, Object> args = new HashMap<>();

        QueueBuilder builder = QueueBuilder.nonDurable(DEAD_Q_1).withArguments(args);
        return builder.build();
    }

    @Bean
    public Binding deadQ1FanoutBinding(@Qualifier("deadQ1") Queue queue,
                                       @Qualifier("deadFanoutExchange") FanoutExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange);
    }

}
