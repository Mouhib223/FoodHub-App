package com.foodhub.delivery.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "drivers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String keycloakId;

    private String firstName;
    private String lastName;
    private String phone;
    private String vehicleType;
    private String vehiclePlate;

    @Enumerated(EnumType.STRING)
    private DriverStatus status;

    private Double currentLatitude;
    private Double currentLongitude;

    private Integer totalDeliveries;
    private Double averageRating;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = DriverStatus.OFFLINE;
        if (totalDeliveries == null) totalDeliveries = 0;
        if (averageRating == null) averageRating = 0.0;
    }
}

