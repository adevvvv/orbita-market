package com.orbitamarket.orders.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orbitamarket.orders.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentResultConsumer {

    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${app.kafka.topics.payment-completed}")
    public void handlePaymentCompleted(String event) {
        try {
            JsonNode json = objectMapper.readTree(event);
            UUID orderId = UUID.fromString(json.get("orderId").asText());
            log.info("Received payment completed for order: {}", orderId);
            orderService.handlePaymentCompleted(orderId);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse payment completed: {}", event, e);
        }
    }

    @KafkaListener(topics = "${app.kafka.topics.payment-failed}")
    public void handlePaymentFailed(String event) {
        try {
            JsonNode json = objectMapper.readTree(event);
            UUID orderId = UUID.fromString(json.get("orderId").asText());
            String reason = json.has("reason") ? json.get("reason").asText() : "UNKNOWN";
            log.info("Received payment failed for order: {}. Reason: {}", orderId, reason);
            orderService.handlePaymentFailed(orderId, reason);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse payment failed: {}", event, e);
        }
    }
}