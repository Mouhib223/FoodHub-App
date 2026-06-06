package com.foodhub.restaurant.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "restaurants")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private String ownerId;   // Keycloak user ID

    private String imageUrl;
    private String coverImageUrl;

    @Column(nullable = false)
    private String address;

    private String city;
    private Double latitude;
    private Double longitude;

    private String phone;
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RestaurantStatus status;

    private Double averageRating;
    private Integer totalRatings;

    private Double deliveryFee;
    private Integer estimatedDeliveryTime;  // in minutes
    private Double minimumOrderAmount;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    private List<Category> categories;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    private List<OpeningHours> openingHours;

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = RestaurantStatus.PENDING;
        if (averageRating == null) averageRating = 0.0;
        if (totalRatings == null) totalRatings = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

