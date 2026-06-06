package com.foodhub.restaurant.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class MenuItemResponse {
    private Long id;
    private Long categoryId;
    private Long restaurantId;
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private boolean available;
    private boolean featured;
    private Integer preparationTime;
    private String allergens;
    private Integer calories;
}

