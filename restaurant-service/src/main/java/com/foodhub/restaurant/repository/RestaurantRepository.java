package com.foodhub.restaurant.repository;

import com.foodhub.restaurant.entity.Restaurant;
import com.foodhub.restaurant.entity.RestaurantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    List<Restaurant> findByStatus(RestaurantStatus status);
    List<Restaurant> findByOwnerId(String ownerId);

    @Query("SELECT r FROM Restaurant r WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(r.city) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Restaurant> searchRestaurants(@Param("query") String query);

    @Query("SELECT r FROM Restaurant r WHERE r.status = 'ACTIVE' AND r.city = :city")
    List<Restaurant> findActiveByCity(@Param("city") String city);
}

