package com.foodhub.restaurant.service;

import com.foodhub.restaurant.dto.*;
import com.foodhub.restaurant.entity.*;
import com.foodhub.restaurant.exception.ResourceNotFoundException;
import com.foodhub.restaurant.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final CategoryRepository categoryRepository;
    private final RabbitTemplate rabbitTemplate;

    public RestaurantResponse createRestaurant(RestaurantRequest request) {
        Restaurant restaurant = Restaurant.builder()
            .name(request.getName())
            .description(request.getDescription())
            .ownerId(request.getOwnerId())
            .imageUrl(request.getImageUrl())
            .coverImageUrl(request.getCoverImageUrl())
            .address(request.getAddress())
            .city(request.getCity())
            .latitude(request.getLatitude())
            .longitude(request.getLongitude())
            .phone(request.getPhone())
            .email(request.getEmail())
            .deliveryFee(request.getDeliveryFee())
            .estimatedDeliveryTime(request.getEstimatedDeliveryTime())
            .minimumOrderAmount(request.getMinimumOrderAmount())
            .status(RestaurantStatus.PENDING)
            .build();

        restaurant = restaurantRepository.save(restaurant);
        log.info("Created restaurant: {}", restaurant.getId());
        return mapToResponse(restaurant);
    }

    @Transactional(readOnly = true)
    public RestaurantResponse getRestaurantById(Long id) {
        return restaurantRepository.findById(id)
            .map(this::mapToResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponse> getAllActiveRestaurants() {
        return restaurantRepository.findByStatus(RestaurantStatus.ACTIVE)
            .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponse> searchRestaurants(String query) {
        return restaurantRepository.searchRestaurants(query)
            .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public RestaurantResponse updateRestaurant(Long id, RestaurantRequest request) {
        Restaurant restaurant = restaurantRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + id));

        restaurant.setName(request.getName());
        restaurant.setDescription(request.getDescription());
        restaurant.setImageUrl(request.getImageUrl());
        restaurant.setCoverImageUrl(request.getCoverImageUrl());
        restaurant.setAddress(request.getAddress());
        restaurant.setCity(request.getCity());
        restaurant.setPhone(request.getPhone());
        restaurant.setEmail(request.getEmail());
        restaurant.setDeliveryFee(request.getDeliveryFee());
        restaurant.setEstimatedDeliveryTime(request.getEstimatedDeliveryTime());
        restaurant.setMinimumOrderAmount(request.getMinimumOrderAmount());

        return mapToResponse(restaurantRepository.save(restaurant));
    }

    public RestaurantResponse updateStatus(Long id, String status) {
        Restaurant restaurant = restaurantRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + id));
        restaurant.setStatus(RestaurantStatus.valueOf(status.toUpperCase()));
        return mapToResponse(restaurantRepository.save(restaurant));
    }

    public void deleteRestaurant(Long id) {
        restaurantRepository.deleteById(id);
    }

    // MenuItem CRUD
    public MenuItemResponse createMenuItem(MenuItemRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
            .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        MenuItem item = MenuItem.builder()
            .category(category)
            .restaurant(restaurant)
            .name(request.getName())
            .description(request.getDescription())
            .price(request.getPrice())
            .imageUrl(request.getImageUrl())
            .available(request.isAvailable())
            .featured(request.isFeatured())
            .preparationTime(request.getPreparationTime())
            .allergens(request.getAllergens())
            .calories(request.getCalories())
            .build();

        return mapMenuItemToResponse(menuItemRepository.save(item));
    }

    @Transactional(readOnly = true)
    public List<MenuItemResponse> getMenuByRestaurant(Long restaurantId) {
        return menuItemRepository.findByRestaurantIdAndAvailable(restaurantId, true)
            .stream().map(this::mapMenuItemToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MenuItemResponse getMenuItemById(Long id) {
        return menuItemRepository.findById(id)
            .map(this::mapMenuItemToResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Menu item not found: " + id));
    }

    // Check item availability — called via Feign from Order Service
    public boolean checkItemAvailability(Long menuItemId) {
        return menuItemRepository.findById(menuItemId)
            .map(MenuItem::isAvailable)
            .orElse(false);
    }

    // Update rating when RestaurantRatedEvent received from RabbitMQ
    @RabbitListener(queues = "restaurant.rating.queue")
    public void handleRestaurantRated(RestaurantRatedEvent event) {
        log.info("Received rating event for restaurant: {}", event.restaurantId());
        Restaurant restaurant = restaurantRepository.findById(event.restaurantId())
            .orElse(null);
        if (restaurant != null) {
            int total = restaurant.getTotalRatings() + 1;
            double newAvg = ((restaurant.getAverageRating() * restaurant.getTotalRatings())
                            + event.rating()) / total;
            restaurant.setTotalRatings(total);
            restaurant.setAverageRating(Math.round(newAvg * 10.0) / 10.0);
            restaurantRepository.save(restaurant);
            log.info("Updated restaurant {} rating to {}", restaurant.getId(), restaurant.getAverageRating());
        }
    }

    private RestaurantResponse mapToResponse(Restaurant r) {
        RestaurantResponse res = new RestaurantResponse();
        res.setId(r.getId());
        res.setName(r.getName());
        res.setDescription(r.getDescription());
        res.setOwnerId(r.getOwnerId());
        res.setImageUrl(r.getImageUrl());
        res.setCoverImageUrl(r.getCoverImageUrl());
        res.setAddress(r.getAddress());
        res.setCity(r.getCity());
        res.setLatitude(r.getLatitude());
        res.setLongitude(r.getLongitude());
        res.setPhone(r.getPhone());
        res.setEmail(r.getEmail());
        res.setStatus(r.getStatus());
        res.setAverageRating(r.getAverageRating());
        res.setTotalRatings(r.getTotalRatings());
        res.setDeliveryFee(r.getDeliveryFee());
        res.setEstimatedDeliveryTime(r.getEstimatedDeliveryTime());
        res.setMinimumOrderAmount(r.getMinimumOrderAmount());
        res.setCreatedAt(r.getCreatedAt());
        return res;
    }

    private MenuItemResponse mapMenuItemToResponse(MenuItem item) {
        MenuItemResponse res = new MenuItemResponse();
        res.setId(item.getId());
        res.setCategoryId(item.getCategory().getId());
        res.setRestaurantId(item.getRestaurant().getId());
        res.setName(item.getName());
        res.setDescription(item.getDescription());
        res.setPrice(item.getPrice());
        res.setImageUrl(item.getImageUrl());
        res.setAvailable(item.isAvailable());
        res.setFeatured(item.isFeatured());
        res.setPreparationTime(item.getPreparationTime());
        res.setAllergens(item.getAllergens());
        res.setCalories(item.getCalories());
        return res;
    }

    public record RestaurantRatedEvent(Long restaurantId, Long userId, Double rating) {}
}

