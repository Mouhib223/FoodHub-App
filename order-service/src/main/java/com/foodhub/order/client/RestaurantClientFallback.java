package com.foodhub.order.client;

import org.springframework.stereotype.Component;

@Component
public class RestaurantClientFallback implements RestaurantClient {

    @Override
    public Boolean checkItemAvailability(Long itemId) {
        return false;  // safe default — item unavailable if restaurant service is down
    }

    @Override
    public MenuItemDto getMenuItem(Long itemId) {
        return null;
    }

    @Override
    public RestaurantDto getRestaurant(Long restaurantId) {
        return null;
    }
}

