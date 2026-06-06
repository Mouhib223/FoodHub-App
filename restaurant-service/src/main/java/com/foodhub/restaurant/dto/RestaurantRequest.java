package com.foodhub.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RestaurantRequest {
    @NotBlank private String name;
    private String description;
    @NotBlank private String ownerId;
    private String imageUrl;
    private String coverImageUrl;
    @NotBlank private String address;
    private String city;
    private Double latitude;
    private Double longitude;
    private String phone;
    private String email;
    private Double deliveryFee;
    private Integer estimatedDeliveryTime;
    private Double minimumOrderAmount;
}

