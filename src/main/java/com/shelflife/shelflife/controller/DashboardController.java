package com.shelflife.shelflife.controller;

import com.shelflife.shelflife.model.FoodItem;
import com.shelflife.shelflife.model.User;
import com.shelflife.shelflife.service.AnalyticsService;
import com.shelflife.shelflife.service.ExpiryAlertService;
import com.shelflife.shelflife.service.FoodItemService;
import com.shelflife.shelflife.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.*;

@Controller
public class DashboardController {

    @Autowired
    private FoodItemService foodItemService;

    @Autowired
    private UserService userService;

    @Autowired
    private ExpiryAlertService expiryAlertService;

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    public String showDashboard(Model model, @AuthenticationPrincipal UserDetails currentUser) {
        if (currentUser != null) {
            User user = userService.findByEmail(currentUser.getUsername());
            if (user != null) {

                List<FoodItem> foodItems = foodItemService.getAllItemsByUser(user);

                Map<Long, String> expiryStatus = new HashMap<>();
                Map<Long, Integer> daysUntilExpiry = new HashMap<>();
                List<FoodItem> expiringSoon = new ArrayList<>();
                List<FoodItem> expiredItems = new ArrayList<>();


                if (foodItems != null && !foodItems.isEmpty() && expiryAlertService != null) {
                    expiringSoon = expiryAlertService.getItemsExpiringSoon(user);
                    expiredItems = expiryAlertService.getExpiredItems(user);


                    for (FoodItem item : foodItems) {
                        if (item != null && item.getExpiryDate() != null) {
                            expiryStatus.put(item.getId(), expiryAlertService.getExpiryStatus(item));
                            daysUntilExpiry.put(item.getId(), expiryAlertService.getDaysUntilExpiry(item));
                        }
                    }
                }

                AnalyticsService.WasteAnalytics analytics = analyticsService.getWasteAnalytics(user, foodItems);
                Map<String, Object> trends = analyticsService.getMonthlyTrends(user, foodItems);


                model.addAttribute("foodItems", foodItems != null ? foodItems : new ArrayList<>());
                model.addAttribute("newItem", new FoodItem());
                model.addAttribute("expiringSoon", expiringSoon);
                model.addAttribute("expiredItems", expiredItems);
                model.addAttribute("expiryStatus", expiryStatus);
                model.addAttribute("daysUntilExpiry", daysUntilExpiry);
                model.addAttribute("analytics", analytics);
                model.addAttribute("trends", trends);

                return "dashboard";
            }
        }
        return "redirect:/login";
    }

    @GetMapping("/inventory")
    public String showInventory(Model model, @AuthenticationPrincipal UserDetails currentUser){
        if (currentUser != null){
            User user = userService.findByEmail(currentUser.getUsername());
            if (user != null){
                List<FoodItem> foodItems = foodItemService.getAllItemsByUser(user);
                Map<Long, String> expiryStatus = new HashMap<>();
                Map<Long, Integer> daysUntilExpiry = new HashMap<>();

                if (foodItems != null && !foodItems.isEmpty() && expiryAlertService != null){
                    for (FoodItem item : foodItems){
                        if (item != null && item.getExpiryDate() != null){
                            expiryStatus.put(item.getId(), expiryAlertService.getExpiryStatus(item));
                            daysUntilExpiry.put(item.getId(), expiryAlertService.getDaysUntilExpiry(item));
                        }
                    }
                }

                model.addAttribute("foodItems", foodItems != null ? foodItems : new ArrayList<>());
                model.addAttribute("expiryStatus", expiryStatus);
                model.addAttribute("daysUntilExpiry", daysUntilExpiry);

                return "inventory";
            }
        }
        return "redirect:/login";
    }

    @GetMapping("/add-item")
    public String showAddItemForm(Model model, @AuthenticationPrincipal UserDetails currentUser){
        if (currentUser != null){
            model.addAttribute("newItem", new FoodItem());
            return "add-item";
        }
        return "redirect:/login";
    }

    @PostMapping("/dashboard/add")
    public String addFoodItem(@Valid @ModelAttribute("newItem") FoodItem item,
                              BindingResult result,
                              @AuthenticationPrincipal UserDetails currentUser,
                              Model model){


        if (item.getPurchaseDate() != null && item.getExpiryDate() != null) {
            if (item.getExpiryDate().isBefore(item.getPurchaseDate())) {
                result.rejectValue("expiryDate", "error.expiryDate", "Expiry date must be after purchase date");
            }
        }

        if (item.getExpiryDate() != null && item.getExpiryDate().isBefore(LocalDate.now())) {
            result.rejectValue("expiryDate", "error.expiryDate", "Expiry date cannot be in the past");
        }


        if (result.hasErrors()) {
            User user = userService.findByEmail(currentUser.getUsername());
            if (user != null) {
                List<FoodItem> foodItems = foodItemService.getAllItemsByUser(user);
                model.addAttribute("foodItems", foodItems != null ? foodItems : new ArrayList<>());
                Map<Long, String> expiryStatus = new HashMap<>();
                Map<Long, Integer> daysUntilExpiry = new HashMap<>();
                List<FoodItem> expiringSoon = new ArrayList<>();
                List<FoodItem> expiredItems = new ArrayList<>();

                if (foodItems != null && !foodItems.isEmpty() && expiryAlertService != null) {
                    expiringSoon = expiryAlertService.getItemsExpiringSoon(user);
                    expiredItems = expiryAlertService.getExpiredItems(user);

                    for (FoodItem existingItem : foodItems) {
                        if (existingItem != null && existingItem.getExpiryDate() != null) {
                            expiryStatus.put(existingItem.getId(), expiryAlertService.getExpiryStatus(existingItem));
                            daysUntilExpiry.put(existingItem.getId(), expiryAlertService.getDaysUntilExpiry(existingItem));
                        }
                    }
                }

                model.addAttribute("foodItems", foodItems !=null ? foodItems : new ArrayList<>());
                model.addAttribute("expiringSoon", expiringSoon);
                model.addAttribute("expiredItems", expiredItems);
                model.addAttribute("expiryStatus", expiryStatus);
                model.addAttribute("daysUntilExpiry", daysUntilExpiry);
            }

            return "dashboard";
        }


        if (currentUser != null) {
            User user = userService.findByEmail(currentUser.getUsername());
            if (user != null) {
                item.setUser(user);
                if (item.getPurchaseDate() == null) {
                    item.setPurchaseDate(LocalDate.now());
                }


                foodItemService.addItem(item);
            }
        }

        return "redirect:/inventory";
    }

    @PostMapping("/dashboard/delete/{id}")
    public String deleteFoodItem(@PathVariable Long id){
        try {
            foodItemService.deleteItem(id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/dashboard";
    }


}