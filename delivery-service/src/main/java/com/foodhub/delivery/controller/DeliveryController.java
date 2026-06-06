package com.foodhub.delivery.controller;

import com.foodhub.delivery.client.OrderClient;
import com.foodhub.delivery.entity.*;
import com.foodhub.delivery.messaging.DeliveryEventListener;
import com.foodhub.delivery.repository.*;
import com.foodhub.delivery.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/delivery")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryRepository deliveryRepository;
    private final DriverRepository driverRepository;
    private final OrderClient orderClient;         // OpenFeign
    private final DeliveryEventListener eventListener;

    @GetMapping("/{deliveryId}")
    public ResponseEntity<Delivery> getDelivery(@PathVariable Long deliveryId) {
        return deliveryRepository.findById(deliveryId)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getDeliveryForOrder(@PathVariable Long orderId) {
        // Feign Scenario 3: Verify order exists via Order Service
        OrderClient.OrderDto order = orderClient.getOrderById(orderId);
        if (order == null) throw new ResourceNotFoundException("Order not found: " + orderId);

        return deliveryRepository.findByOrderId(orderId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{deliveryId}/status")
    public ResponseEntity<Delivery> updateStatus(
            @PathVariable Long deliveryId,
            @RequestParam String status) {

        Delivery delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));

        DeliveryStatus newStatus = DeliveryStatus.valueOf(status.toUpperCase());
        delivery.setStatus(newStatus);

        if (newStatus == DeliveryStatus.DELIVERED) {
            delivery.setDeliveredAt(java.time.LocalDateTime.now());
            eventListener.publishDeliveryCompleted(
                delivery.getId(), delivery.getOrderId(),
                delivery.getDriver() != null ? delivery.getDriver().getId() : null);
        }

        return ResponseEntity.ok(deliveryRepository.save(delivery));
    }

    // Driver CRUD
    @PostMapping("/drivers")
    public ResponseEntity<Driver> createDriver(@RequestBody Driver driver) {
        return ResponseEntity.ok(driverRepository.save(driver));
    }

    @GetMapping("/drivers")
    public ResponseEntity<List<Driver>> getAllDrivers() {
        return ResponseEntity.ok(driverRepository.findAll());
    }

    @PatchMapping("/drivers/{driverId}/status")
    public ResponseEntity<Driver> updateDriverStatus(
            @PathVariable Long driverId,
            @RequestParam String status) {
        Driver driver = driverRepository.findById(driverId)
            .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));
        driver.setStatus(DriverStatus.valueOf(status.toUpperCase()));
        return ResponseEntity.ok(driverRepository.save(driver));
    }
}

