package com.foodhub.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddressRequest {
    @NotBlank(message = "Label is required")
    private String label;
    @NotBlank(message = "Street is required")
    private String street;
    @NotBlank(message = "City is required")
    private String city;
    private String state;
    @NotBlank(message = "Postal code is required")
    private String postalCode;
    private String country;
    private Double latitude;
    private Double longitude;
    private boolean isDefault;
}

