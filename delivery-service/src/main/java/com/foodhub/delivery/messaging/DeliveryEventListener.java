package com.foodhub.delivery.messaging;

import com.foodhub.delivery.entity.Delivery;
import com.foodhub.delivery.entity.DeliveryStatus;
import com.foodhub.delivery.repository.DeliveryRepository;
import com.foodhub.delivery.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeliveryEventListener {

    private final DeliveryRepository deliveryRepository;
    private final DriverRepository driverRepository;
    private final RabbitTemplate rabbitTemplate;

    // RabbitMQ Scenario 2: Listen to OrderAcceptedEvent, create delivery assignment
    @RabbitListener(queues = "order.accepted.queue")
    public void handleOrderAccepted(OrderAcceptedEvent event) {
        log.info("Received OrderAcceptedEvent for order: {}", event.orderId());

        // Find an available driver (simplified — real would use location matching)
        driverRepository.findFirstByStatus(com.foodhub.delivery.entity.DriverStatus.ONLINE)
            .ifPresent(driver -> {
                Delivery delivery = Delivery.builder()
                    .orderId(event.orderId())
                    .driver(driver)
                    .status(DeliveryStatus.ASSIGNED)
                    .dropLatitude(event.latitude())
                    .dropLongitude(event.longitude())
                    .estimatedAt(LocalDateTime.now().plusMinutes(30))
                    .build();

                deliveryRepository.save(delivery);
                log.info("Assigned delivery for order {} to driver {}", event.orderId(), driver.getId());

                // Publish DeliveryAssignedEvent
                rabbitTemplate.convertAndSend("foodhub.exchange", "delivery.assigned",
                    new DeliveryAssignedEvent(delivery.getId(), event.orderId(), driver.getId()));
            });
    }

    // RabbitMQ Scenario 3: Publish DeliveryCompletedEvent when delivery is done
    public void publishDeliveryCompleted(Long deliveryId, Long orderId, Long driverId) {
        rabbitTemplate.convertAndSend("foodhub.exchange", "delivery.completed",
            new DeliveryCompletedEvent(deliveryId, orderId, driverId));
        log.info("Published DeliveryCompletedEvent for delivery: {}", deliveryId);
    }

    public record OrderAcceptedEvent(Long orderId, Long restaurantId, String city,
                                     Double latitude, Double longitude) {}
    public record DeliveryAssignedEvent(Long deliveryId, Long orderId, Long driverId) {}
    public record DeliveryCompletedEvent(Long deliveryId, Long orderId, Long driverId) {}
}

