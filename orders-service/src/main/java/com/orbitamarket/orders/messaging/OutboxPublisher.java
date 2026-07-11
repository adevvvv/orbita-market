package com.orbitamarket.orders.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orbitamarket.orders.model.OutboxEvent;
import com.orbitamarket.orders.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topics.payment-request}")
    private String paymentRequestTopic;

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxRepository.findByStatusOrderByCreatedAtAsc("PENDING");

        for (OutboxEvent event : pendingEvents) {
            try {
                kafkaTemplate.send(paymentRequestTopic, event.getOrderId().toString(), event.getPayload())
                        .get();

                event.setStatus("SENT");
                event.setProcessedAt(LocalDateTime.now());
                outboxRepository.save(event);

                log.info("Published outbox event for order: {}", event.getOrderId());
            } catch (Exception e) {
                log.error("Failed to publish outbox event for order: {}", event.getOrderId(), e);
            }
        }
    }
}