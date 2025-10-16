package com.shelflife.shelflife.repository;

import com.shelflife.shelflife.model.FoodItem;
import com.shelflife.shelflife.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FoodItemRepository extends JpaRepository<FoodItem, Long> {
    List<FoodItem> findByUser(User user);
}
