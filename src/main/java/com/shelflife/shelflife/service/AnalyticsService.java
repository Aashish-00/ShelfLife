package com.shelflife.shelflife.service;

import com.shelflife.shelflife.model.FoodItem;
import com.shelflife.shelflife.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    @Autowired
    private ExpiryAlertService expiryAlertService;

    // Static nested class for analytics data
    public static class WasteAnalytics {
        private final int totalItems;
        private final int expiredItemsCount;
        private final int expiringSoonCount;
        private final double wastePercentage;
        private final Map<String, Long> itemsByCategory;


        public WasteAnalytics(int totalItems, int expiredItemsCount, int expiringSoonCount,
                              Map<String, Long> itemsByCategory) {
            this.totalItems = totalItems;
            this.expiredItemsCount = expiredItemsCount;
            this.expiringSoonCount = expiringSoonCount;
            this.itemsByCategory = itemsByCategory != null ? itemsByCategory : new HashMap<>();

            // Calculate derived metrics
            this.wastePercentage = totalItems > 0 ? (double) expiredItemsCount / totalItems * 100 : 0.0;

        }

        public int getTotalItems() {
            return totalItems;
        }

        public int getExpiringSoonCount() {
            return expiringSoonCount;
        }

        public int getExpiredItemsCount() {
            return expiredItemsCount;
        }

        public double getWastePercentage() {
            return wastePercentage;
        }

        public Map<String, Long> getItemsByCategory() {
            return itemsByCategory;
        }
    }

    public WasteAnalytics getWasteAnalytics(User user, List<FoodItem> foodItems) {
        if (foodItems == null || foodItems.isEmpty()) {
            return new WasteAnalytics(0, 0, 0, new HashMap<>());
        }

        int totalItems = foodItems.size();
        int expiredItems = (int) foodItems.stream()
                .filter(item -> item.getExpiryDate() != null && item.getExpiryDate().isBefore(LocalDate.now()))
                .count();

        int expiringSoon = expiryAlertService != null ? expiryAlertService.getItemsExpiringSoon(user).size() : 0;

        // Category analysis
        Map<String, Long> itemsByCategory = foodItems.stream()
                .collect(Collectors.groupingBy(
                        FoodItem::getCategory,
                        Collectors.counting()
                ));

        return new WasteAnalytics(totalItems, expiredItems, expiringSoon, itemsByCategory);
    }

    public Map<String, Object> getMonthlyTrends(User user, List<FoodItem> foodItems) {
        Map<String, Object> trends = new HashMap<>();

        if (foodItems == null || foodItems.isEmpty()) {
            trends.put("monthlyBreakdown", new HashMap<>());
            trends.put("mostCommonCategory", "None");
            trends.put("averageItemsPerMonth", 0.0);
            return trends;
        }

        // Group items by month for trend analysis
        Map<String, Long> monthlyItems = foodItems.stream()
                .filter(item -> item.getPurchaseDate() != null)
                .collect(Collectors.groupingBy(
                        item -> item.getPurchaseDate().getMonth().toString(),
                        Collectors.counting()
                ));

        trends.put("monthlyBreakdown", monthlyItems);
        trends.put("mostCommonCategory", getMostCommonCategory(foodItems));
        trends.put("averageItemsPerMonth", calculateAveragePerMonth(foodItems));

        return trends;
    }

    private String getMostCommonCategory(List<FoodItem> foodItems) {
        if (foodItems == null || foodItems.isEmpty()) return "None";

        return foodItems.stream()
                .collect(Collectors.groupingBy(FoodItem::getCategory, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("None");
    }

    private double calculateAveragePerMonth(List<FoodItem> foodItems) {
        if (foodItems == null || foodItems.isEmpty()) return 0.0;

        long distinctMonths = foodItems.stream()
                .filter(item -> item.getPurchaseDate() != null)
                .map(item -> item.getPurchaseDate().getMonth())
                .distinct()
                .count();

        return distinctMonths > 0 ? (double) foodItems.size() / distinctMonths : foodItems.size();
    }
}