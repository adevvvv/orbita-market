package com.orbitamarket.payments.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orbitamarket.payments.exception.InsufficientBalanceException;
import com.orbitamarket.payments.messaging.dto.PaymentCompletedEvent;
import com.orbitamarket.payments.messaging.dto.PaymentFailedEvent;
import com.orbitamarket.payments.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentRequestConsumer {

    private final AccountService accountService;
    private final PaymentEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${app.kafka.topics.payment-request}")
    @Transactional
    public void consumePaymentRequest(ConsumerRecord<String, String> record) {
        try {
            JsonNode event = objectMapper.readTree(record.value());

            UUID orderId = UUID.fromString(event.get("order_id").asText());
            UUID eventId = UUID.fromString(event.get("event_id").asText());
            String userId = event.get("user_id").asText();
            Integer amount = event.get("amount").asInt();

            log.info("Received payment request for order: {}, user: {}, amount: {}", orderId, userId, amount);

            try {
                accountService.processPayment(orderId, eventId, userId, amount);

                PaymentCompletedEvent completedEvent = PaymentCompletedEvent.builder()
                        .eventId(UUID.randomUUID())
                        .orderId(orderId)
                        .userId(userId)
                        .amount(amount)
                        .status("PAID")
                        .build();
                eventPublisher.publishPaymentCompleted(completedEvent);

            } catch (InsufficientBalanceException e) {
                PaymentFailedEvent failedEvent = PaymentFailedEvent.builder()
                        .eventId(UUID.randomUUID())
                        .orderId(orderId)
                        .userId(userId)
                        .reason("INSUFFICIENT_BALANCE")
                        .build();
                eventPublisher.publishPaymentFailed(failedEvent);

            } catch (Exception e) {
                log.error("Error processing payment for order: {}", orderId, e);
                PaymentFailedEvent failedEvent = PaymentFailedEvent.builder()
                        .eventId(UUID.randomUUID())
                        .orderId(orderId)
                        .userId(userId)
                        .reason("INTERNAL_ERROR")
                        .build();
                eventPublisher.publishPaymentFailed(failedEvent);
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to parse payment request: {}", record.value(), e);
        }
    }
}