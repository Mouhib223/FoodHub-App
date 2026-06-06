package com.foodhub.user.dto;

import lombok.Data;

@Data
public class AddressResponse {
    private Long id;
    private String label;
    private String street;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private Double latitude;
    private Double longitude;
    private boolean isDefault;
}

