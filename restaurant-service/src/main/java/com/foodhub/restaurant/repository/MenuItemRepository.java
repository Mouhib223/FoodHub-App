package com.foodhub.restaurant.repository;

import com.foodhub.restaurant.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByRestaurantId(Long restaurantId);
    List<MenuItem> findByCategoryId(Long categoryId);
    List<MenuItem> findByRestaurantIdAndAvailable(Long restaurantId, boolean available);
}

