package com.shelflife.shelflife.service;

import com.shelflife.shelflife.model.FoodItem;
import com.shelflife.shelflife.model.User;
import com.shelflife.shelflife.repository.FoodItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ExpiryAlertService {


    @Autowired
    private FoodItemRepository foodItemRepository;

    public List<FoodItem> getExpiringItems(User user, int daysThreshold){
        LocalDate thresholdDate = LocalDate.now().plusDays(daysThreshold);
        return foodItemRepository.findByUserAndExpiryDateBetween(
                user, LocalDate.now(), thresholdDate);
    }

    public List<FoodItem> getExpiredItems(User user){
        return foodItemRepository.findByUserAndExpiryDateBefore(
                user, LocalDate.now());
    }

    public List<FoodItem> getItemsExpiringSoon(User user){
        return getExpiringItems(user, 3);
    }


    public String getExpiryStatus(FoodItem item){
        LocalDate today = LocalDate.now();
        LocalDate expiryDate = item.getExpiryDate();

        if (expiryDate.isBefore(today)){
            return "EXPIRED";
        } else if (expiryDate.isEqual(today)) {
            return "EXPIRES_TODAY";
        } else if (expiryDate.isBefore(today.plusDays(3))){
            return "EXPIRES_SOON";
        } else {
            return "FRESH";
        }
    }

    public int getDaysUntilExpiry(FoodItem item){
        return (int) ChronoUnit.DAYS.between(
                LocalDate.now(), item.getExpiryDate());
    }
}
