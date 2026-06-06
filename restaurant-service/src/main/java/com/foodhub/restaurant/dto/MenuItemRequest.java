package com.foodhub.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class MenuItemRequest {
    @NotNull private Long categoryId;
    @NotNull private Long restaurantId;
    @NotBlank private String name;
    private String description;
    @NotNull private BigDecimal price;
    private String imageUrl;
    private boolean available = true;
    private boolean featured = false;
    private Integer preparationTime;
    private String allergens;
    private Integer calories;
}

