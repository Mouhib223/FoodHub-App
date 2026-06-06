package com.foodhub.restaurant.dto;

import com.foodhub.restaurant.entity.RestaurantStatus;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class RestaurantResponse {
    private Long id;
    private String name;
    private String description;
    private String ownerId;
    private String imageUrl;
    private String coverImageUrl;
    private String address;
    private String city;
    private Double latitude;
    private Double longitude;
    private String phone;
    private String email;
    private RestaurantStatus status;
    private Double averageRating;
    private Integer totalRatings;
    private Double deliveryFee;
    private Integer estimatedDeliveryTime;
    private Double minimumOrderAmount;
    private LocalDateTime createdAt;
}

