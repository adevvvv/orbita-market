package com.orbitamarket.orders.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Value("${app.kafka.topics.payment-request}")
    private String paymentRequestTopic;

    @Bean
    public NewTopic paymentRequestTopic() {
        return TopicBuilder.name(paymentRequestTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}