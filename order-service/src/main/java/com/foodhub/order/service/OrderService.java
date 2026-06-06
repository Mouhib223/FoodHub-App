package com.foodhub.order.service;

import com.foodhub.order.client.RestaurantClient;
import com.foodhub.order.client.UserClient;
import com.foodhub.order.config.RabbitMQConfig;
import com.foodhub.order.dto.*;
import com.foodhub.order.entity.*;
import com.foodhub.order.exception.ResourceNotFoundException;
import com.foodhub.order.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final RestaurantClient restaurantClient;   // OpenFeign
    private final UserClient userClient;               // OpenFeign
    private final RabbitTemplate rabbitTemplate;

    // ===================== CART MANAGEMENT =====================

    public CartResponse addToCart(Long userId, CartItemRequest request) {
        // Feign Scenario 1: Check item availability from Restaurant Service
        Boolean available = restaurantClient.checkItemAvailability(request.getMenuItemId());
        if (available == null || !available) {
            throw new IllegalArgumentException("Item is not available: " + request.getMenuItemId());
        }

        // Feign Scenario 1 (extended): Get item details from Restaurant Service
        RestaurantClient.MenuItemDto item = restaurantClient.getMenuItem(request.getMenuItemId());
        if (item == null) throw new ResourceNotFoundException("Menu item not found");

        Cart cart = cartRepository.findByUserId(userId)
            .orElseGet(() -> Cart.builder().userId(userId).build());

        // If different restaurant, clear cart
        if (cart.getRestaurantId() != null && !cart.getRestaurantId().equals(item.restaurantId())) {
            cart.getItems().clear();
        }
        cart.setRestaurantId(item.restaurantId());

        // Check if item already in cart
        cart.getItems().stream()
            .filter(ci -> ci.getMenuItemId().equals(request.getMenuItemId()))
            .findFirst()
            .ifPresentOrElse(
                ci -> ci.setQuantity(ci.getQuantity() + request.getQuantity()),
                () -> {
                    CartItem cartItem = CartItem.builder()
                        .cart(cart)
                        .menuItemId(item.id())
                        .menuItemName(item.name())
                        .unitPrice(item.price())
                        .quantity(request.getQuantity())
                        .specialInstructions(request.getSpecialInstructions())
                        .build();
                    cart.getItems().add(cartItem);
                }
            );

        Cart saved = cartRepository.save(cart);
        return mapCartToResponse(saved);
    }

    public CartResponse getCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
            .orElseGet(() -> Cart.builder().userId(userId).build());
        return mapCartToResponse(cart);
    }

    public void clearCart(Long userId) {
        cartRepository.findByUserId(userId).ifPresent(cart -> {
            cart.getItems().clear();
            cartRepository.save(cart);
        });
    }

    // ===================== ORDER MANAGEMENT =====================

    public OrderResponse placeOrder(Long userId, PlaceOrderRequest request) {
        Cart cart = cartRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("Cart is empty"));

        if (cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        // Feign Scenario 2: Get delivery address from User Service
        UserClient.AddressDto deliveryAddress = userClient.getUserAddresses(userId)
            .stream()
            .filter(a -> a.id().equals(request.getAddressId()))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        // Feign: Get restaurant details for delivery fee
        RestaurantClient.RestaurantDto restaurant = restaurantClient.getRestaurant(cart.getRestaurantId());

        // Build order items
        List<OrderItem> orderItems = cart.getItems().stream().map(ci -> {
            BigDecimal itemTotal = ci.getUnitPrice().multiply(BigDecimal.valueOf(ci.getQuantity()));
            return OrderItem.builder()
                .menuItemId(ci.getMenuItemId())
                .menuItemName(ci.getMenuItemName())
                .quantity(ci.getQuantity())
                .unitPrice(ci.getUnitPrice())
                .totalPrice(itemTotal)
                .specialInstructions(ci.getSpecialInstructions())
                .build();
        }).collect(Collectors.toList());

        BigDecimal subtotal = orderItems.stream()
            .map(OrderItem::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal deliveryFee = restaurant != null
            ? BigDecimal.valueOf(restaurant.deliveryFee() != null ? restaurant.deliveryFee() : 0)
            : BigDecimal.ZERO;

        Order order = Order.builder()
            .userId(userId)
            .restaurantId(cart.getRestaurantId())
            .status(OrderStatus.PENDING)
            .subtotal(subtotal)
            .deliveryFee(deliveryFee)
            .total(subtotal.add(deliveryFee))
            .deliveryStreet(deliveryAddress.street())
            .deliveryCity(deliveryAddress.city())
            .deliveryPostalCode(deliveryAddress.postalCode())
            .deliveryLatitude(deliveryAddress.latitude())
            .deliveryLongitude(deliveryAddress.longitude())
            .specialInstructions(request.getSpecialInstructions())
            .estimatedDeliveryAt(LocalDateTime.now().plusMinutes(
                restaurant != null && restaurant.estimatedDeliveryTime() != null
                    ? restaurant.estimatedDeliveryTime() : 45))
            .build();

        order = orderRepository.save(order);
        final Order savedOrder = order;

        // Set back-reference on items and save
        orderItems.forEach(item -> item.setOrder(savedOrder));
        order.setItems(orderItems);
        order = orderRepository.save(order);

        // Clear the cart
        cart.getItems().clear();
        cart.setRestaurantId(null);
        cartRepository.save(cart);

        // Publish OrderCreatedEvent via RabbitMQ
        OrderCreatedEvent event = new OrderCreatedEvent(
            order.getId(), order.getUserId(), order.getRestaurantId(),
            order.getTotal(), order.getDeliveryCity()
        );
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ORDER_CREATED_KEY, event);
        log.info("Published OrderCreatedEvent for order: {}", order.getId());

        return mapOrderToResponse(order);
    }

    public OrderResponse updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        OrderStatus newStatus = OrderStatus.valueOf(status.toUpperCase());
        order.setStatus(newStatus);
        order = orderRepository.save(order);

        // Publish status update event
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ORDER_STATUS_KEY,
            new OrderStatusUpdatedEvent(order.getId(), order.getUserId(), newStatus.name()));

        // If accepted, publish OrderAcceptedEvent for Delivery Service
        if (newStatus == OrderStatus.CONFIRMED) {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ORDER_ACCEPTED_KEY,
                new OrderAcceptedEvent(order.getId(), order.getRestaurantId(),
                    order.getDeliveryCity(), order.getDeliveryLatitude(), order.getDeliveryLongitude()));
            log.info("Published OrderAcceptedEvent for order: {}", order.getId());
        }

        return mapOrderToResponse(order);
    }

    public OrderResponse rateRestaurant(Long orderId, RatingRequest request) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new IllegalArgumentException("Can only rate delivered orders");
        }

        // Publish RestaurantRatedEvent
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.RESTAURANT_RATED_KEY,
            new RestaurantRatedEvent(order.getRestaurantId(), order.getUserId(), request.getRating()));
        log.info("Published RestaurantRatedEvent for restaurant: {}", order.getRestaurantId());

        return mapOrderToResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
            .map(this::mapOrderToResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUser(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
            .stream().map(this::mapOrderToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByRestaurant(Long restaurantId) {
        return orderRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId)
            .stream().map(this::mapOrderToResponse).collect(Collectors.toList());
    }

    // ===================== MAPPERS =====================

    private OrderResponse mapOrderToResponse(Order o) {
        OrderResponse r = new OrderResponse();
        r.setId(o.getId());
        r.setUserId(o.getUserId());
        r.setRestaurantId(o.getRestaurantId());
        r.setStatus(o.getStatus().name());
        r.setSubtotal(o.getSubtotal());
        r.setDeliveryFee(o.getDeliveryFee());
        r.setTotal(o.getTotal());
        r.setDeliveryCity(o.getDeliveryCity());
        r.setSpecialInstructions(o.getSpecialInstructions());
        r.setCreatedAt(o.getCreatedAt());
        r.setEstimatedDeliveryAt(o.getEstimatedDeliveryAt());
        if (o.getItems() != null) {
            r.setItems(o.getItems().stream().map(item -> {
                OrderItemResponse ir = new OrderItemResponse();
                ir.setMenuItemId(item.getMenuItemId());
                ir.setMenuItemName(item.getMenuItemName());
                ir.setQuantity(item.getQuantity());
                ir.setUnitPrice(item.getUnitPrice());
                ir.setTotalPrice(item.getTotalPrice());
                return ir;
            }).collect(Collectors.toList()));
        }
        return r;
    }

    private CartResponse mapCartToResponse(Cart cart) {
        CartResponse r = new CartResponse();
        r.setUserId(cart.getUserId());
        r.setRestaurantId(cart.getRestaurantId());
        if (cart.getItems() != null) {
            r.setItems(cart.getItems().stream().map(ci -> {
                CartItemResponse cir = new CartItemResponse();
                cir.setMenuItemId(ci.getMenuItemId());
                cir.setMenuItemName(ci.getMenuItemName());
                cir.setUnitPrice(ci.getUnitPrice());
                cir.setQuantity(ci.getQuantity());
                return cir;
            }).collect(Collectors.toList()));
            BigDecimal total = cart.getItems().stream()
                .map(ci -> ci.getUnitPrice().multiply(BigDecimal.valueOf(ci.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            r.setTotal(total);
        }
        return r;
    }

    // Event records
    public record OrderCreatedEvent(Long orderId, Long userId, Long restaurantId,
                                    BigDecimal total, String deliveryCity) {}
    public record OrderAcceptedEvent(Long orderId, Long restaurantId, String city,
                                     Double latitude, Double longitude) {}
    public record OrderStatusUpdatedEvent(Long orderId, Long userId, String newStatus) {}
    public record RestaurantRatedEvent(Long restaurantId, Long userId, Double rating) {}
}

