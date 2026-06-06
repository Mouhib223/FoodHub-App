package com.foodhub.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "restaurant-service", fallback = RestaurantClientFallback.class)
public interface RestaurantClient {

    @GetMapping("/api/v1/restaurants/menu/{itemId}/availability")
    Boolean checkItemAvailability(@PathVariable Long itemId);

    @GetMapping("/api/v1/restaurants/menu/{itemId}")
    MenuItemDto getMenuItem(@PathVariable Long itemId);

    @GetMapping("/api/v1/restaurants/{restaurantId}")
    RestaurantDto getRestaurant(@PathVariable Long restaurantId);

    record MenuItemDto(Long id, String name, java.math.BigDecimal price, boolean available,
                       Long restaurantId, Integer preparationTime) {}

    record RestaurantDto(Long id, String name, Double deliveryFee,
                         Integer estimatedDeliveryTime, Double minimumOrderAmount) {}
}

