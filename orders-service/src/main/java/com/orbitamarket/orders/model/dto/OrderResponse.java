package com.orbitamarket.orders.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.orbitamarket.orders.model.OrderStatus;
import com.orbitamarket.orders.model.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private UUID orderId;
    private String userId;
    private ProductType productType;
    private OrderStatus status;
    private Integer price;
    private String payload;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;

    private String failureReason;
}