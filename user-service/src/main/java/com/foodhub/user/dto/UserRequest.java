package com.foodhub.user.dto;

import com.foodhub.user.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserRequest {

    @NotBlank(message = "Keycloak ID is required")
    private String keycloakId;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Email(message = "Valid email required")
    @NotBlank(message = "Email is required")
    private String email;

    private String phone;
    private UserRole role;
    private String profileImageUrl;
}

