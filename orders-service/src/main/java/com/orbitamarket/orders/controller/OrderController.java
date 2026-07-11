package com.orbitamarket.orders.controller;

import com.orbitamarket.orders.model.Order;
import com.orbitamarket.orders.model.dto.CreateOrderRequest;
import com.orbitamarket.orders.model.dto.OrderResponse;
import com.orbitamarket.orders.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CreateOrderRequest request) {
        Order order = orderService.createOrder(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(order));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrders(@RequestHeader("X-User-Id") String userId) {
        List<Order> orders = orderService.getUserOrders(userId);
        List<OrderResponse> responses = orders.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable UUID orderId) {
        Order order = orderService.getOrder(orderId, userId);
        return ResponseEntity.ok(toResponse(order));
    }

    private OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .userId(order.getUserId())
                .productType(order.getProductType())
                .status(order.getStatus())
                .price(order.getPrice())
                .payload(order.getPayload())
                .createdAt(order.getCreatedAt())
                .failureReason(order.getFailureReason())
                .build();
    }
}