package com.foodhub.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

@FeignClient(name = "user-service", fallback = UserClientFallback.class)
public interface UserClient {

    @GetMapping("/api/v1/users/{userId}/addresses")
    List<AddressDto> getUserAddresses(@PathVariable Long userId);

    @GetMapping("/api/v1/users/{userId}")
    UserDto getUserById(@PathVariable Long userId);

    record AddressDto(Long id, String label, String street, String city,
                      String postalCode, Double latitude, Double longitude, boolean isDefault) {}

    record UserDto(Long id, String firstName, String lastName, String email, String phone) {}
}

