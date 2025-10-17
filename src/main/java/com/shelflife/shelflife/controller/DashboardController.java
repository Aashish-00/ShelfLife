package com.shelflife.shelflife.controller;

import com.shelflife.shelflife.model.FoodItem;
import com.shelflife.shelflife.model.User;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {

    @Autowired
    private FoodItemService foodItemService;

    @Autowired
    private UserService userService;

    @Autowired
    private ExpiryAlertService expiryAlertService;

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


                model.addAttribute("foodItems", foodItems != null ? foodItems : new ArrayList<>());
                model.addAttribute("newItem", new FoodItem());
                model.addAttribute("expiringSoon", expiringSoon);
                model.addAttribute("expiredItems", expiredItems);
                model.addAttribute("expiryStatus", expiryStatus);
                model.addAttribute("daysUntilExpiry", daysUntilExpiry);

                return "dashboard";
            }
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
            System.out.println("=== VALIDATION ERRORS ===");
            result.getFieldErrors().forEach(error -> {
                System.out.println("Field: " + error.getField() + " - Error: " + error.getDefaultMessage());
            });


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
                System.out.println("Food item added successfully: " + item.getName());
            }
        }

        return "redirect:/dashboard";
    }

    @PostMapping("/dashboard/delete/{id}")
    public String deleteFoodItem(@PathVariable Long id){
        System.out.println("Deleting food item with ID: " + id);

        try {
            foodItemService.deleteItem(id);
            System.out.println("Food item deleted successfully: " + id);
        } catch (Exception e) {
            System.out.println("Error deleting food item: " + e.getMessage());
            e.printStackTrace();
        }

        return "redirect:/dashboard";
    }
}