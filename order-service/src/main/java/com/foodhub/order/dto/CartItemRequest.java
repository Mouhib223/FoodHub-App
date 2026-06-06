package com.foodhub.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartItemRequest {
    @NotNull private Long menuItemId;
    @NotNull private Integer quantity;
    private String specialInstructions;
}

