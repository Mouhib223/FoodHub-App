package com.foodhub.order.client;

import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.List;

@Component
public class UserClientFallback implements UserClient {

    @Override
    public List<AddressDto> getUserAddresses(Long userId) {
        return Collections.emptyList();
    }

    @Override
    public UserDto getUserById(Long userId) {
        return null;
    }
}

