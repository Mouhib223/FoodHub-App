package com.foodhub.restaurant.repository;

import com.foodhub.restaurant.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByRestaurantIdOrderByDisplayOrder(Long restaurantId);
}

