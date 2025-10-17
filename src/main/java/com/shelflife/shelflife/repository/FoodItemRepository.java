package com.shelflife.shelflife.repository;

import com.shelflife.shelflife.model.FoodItem;
import com.shelflife.shelflife.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface FoodItemRepository extends JpaRepository<FoodItem, Long> {
    List<FoodItem> findByUser(User user);

    List<FoodItem> findByUserAndExpiryDateBetween(User user, LocalDate startDate, LocalDate endDate);

    List<FoodItem> findByUserAndExpiryDateBefore(User user,LocalDate date);

    @Query("SELECT f FROM FoodItem f WHERE f.user = :user AND f.expiryDate <= :thresholdDate ORDER BY f.expiryDate ASC")

    List<FoodItem> findExpiringItemsByUser(@Param("user") User user, @Param("thresholdDate") LocalDate thresholdDate);
}
