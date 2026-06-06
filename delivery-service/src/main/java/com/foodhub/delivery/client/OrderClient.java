package com.foodhub.delivery.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// Feign Scenario 3: Delivery Service fetches order details from Order Service
@FeignClient(name = "order-service")
public interface OrderClient {

    @GetMapping("/api/v1/orders/{orderId}")
    OrderDto getOrderById(@PathVariable("orderId") Long orderId);

    record OrderDto(Long id, Long userId, Long restaurantId, String status,
                    java.math.BigDecimal total, String deliveryCity,
                    Double deliveryLatitude, Double deliveryLongitude) {}
}

