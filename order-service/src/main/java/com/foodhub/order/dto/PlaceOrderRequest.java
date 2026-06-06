package com.foodhub.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PlaceOrderRequest {
    @NotNull private Long addressId;
    private String specialInstructions;
}

