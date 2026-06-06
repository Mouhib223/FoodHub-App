package com.foodhub.user.dto;

import com.foodhub.user.entity.UserRole;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;


@Data
public class UserResponse {
    private Long id;
    private String keycloakId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private UserRole role;
    private boolean active;
    private String profileImageUrl;
    private List<AddressResponse> addresses;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

