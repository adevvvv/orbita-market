package com.orbitamarket.orders.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.orbitamarket.orders.model.ProductType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreateOrderRequest {
    @NotNull(message = "Product type is required")
    @JsonProperty("product_type")
    private ProductType productType;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than zero")
    private Integer price;

    private String payload;
}