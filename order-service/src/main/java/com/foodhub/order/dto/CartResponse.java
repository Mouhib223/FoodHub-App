package com.foodhub.order.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CartResponse {
    private Long userId;
    private Long restaurantId;
    private List<CartItemResponse> items;
    private BigDecimal total;
}

