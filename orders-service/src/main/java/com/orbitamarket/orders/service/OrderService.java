package com.orbitamarket.orders.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orbitamarket.orders.exception.InvalidOrderException;
import com.orbitamarket.orders.exception.OrderNotFoundException;
import com.orbitamarket.orders.model.*;
import com.orbitamarket.orders.model.dto.CreateOrderRequest;
import com.orbitamarket.orders.repository.OrderRepository;
import com.orbitamarket.orders.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public Order createOrder(String userId, CreateOrderRequest request) {
        log.info("Creating order for user: {}, type: {}, price: {}", userId, request.getProductType(), request.getPrice());
        validateOrder(request);

        Order order = new Order();
        order.setUserId(userId);
        order.setProductType(request.getProductType());
        order.setPrice(request.getPrice());
        order.setPayload(request.getPayload());
        order.setStatus(OrderStatus.CREATED);

        Order savedOrder = orderRepository.save(order);
        log.info("Order saved with id: {}", savedOrder.getOrderId());

        // Create outbox event for payment
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("event_id", UUID.randomUUID().toString());
            payload.put("order_id", savedOrder.getOrderId().toString());
            payload.put("user_id", userId);
            payload.put("amount", request.getPrice());
            payload.put("occurred_at", LocalDateTime.now().toString());

            String payloadJson = objectMapper.writeValueAsString(payload);
            log.info("Outbox payload: {}", payloadJson);

            OutboxEvent outboxEvent = new OutboxEvent();
            outboxEvent.setOrderId(savedOrder.getOrderId());
            outboxEvent.setEventType("ORDER_PAYMENT_REQUESTED");
            outboxEvent.setPayload(payloadJson);
            outboxRepository.save(outboxEvent);
            log.info("Outbox event saved for order: {}", savedOrder.getOrderId());

            // Update order status
            savedOrder.setStatus(OrderStatus.PAYMENT_PENDING);
            orderRepository.save(savedOrder);
            log.info("Order status updated to PAYMENT_PENDING for order: {}", savedOrder.getOrderId());

        } catch (Exception e) {
            log.error("Failed to create outbox event for order: {}", savedOrder.getOrderId(), e);
            throw new RuntimeException("Failed to process order: " + e.getMessage(), e);
        }

        return savedOrder;
    }

    public List<Order> getUserOrders(String userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Order getOrder(UUID orderId, String userId) {
        return orderRepository.findByOrderIdAndUserId(orderId, userId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
    }

    @Transactional
    public void handlePaymentCompleted(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);
        log.info("Order: {} marked as PAID", orderId);
    }

    @Transactional
    public void handlePaymentFailed(UUID orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
        order.setStatus(OrderStatus.PAYMENT_FAILED);
        order.setFailureReason(reason);
        orderRepository.save(order);
        log.info("Order: {} marked as PAYMENT_FAILED. Reason: {}", orderId, reason);
    }

    private void validateOrder(CreateOrderRequest request) {
        if (request.getProductType() == null) {
            throw new InvalidOrderException("UNKNOWN_PRODUCT_TYPE", "Product type is required");
        }
        if (request.getPrice() == null || request.getPrice() <= 0) {
            throw new InvalidOrderException("INVALID_PRICE", "Price must be greater than zero");
        }
        if (request.getPayload() == null || request.getPayload().isEmpty()) {
            throw new InvalidOrderException("INVALID_PAYLOAD", "Payload is required");
        }
    }
}