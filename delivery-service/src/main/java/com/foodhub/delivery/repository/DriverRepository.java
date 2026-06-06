package com.foodhub.delivery.repository;

import com.foodhub.delivery.entity.Driver;
import com.foodhub.delivery.entity.DriverStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DriverRepository extends JpaRepository<Driver, Long> {
    Optional<Driver> findFirstByStatus(DriverStatus status);
}

