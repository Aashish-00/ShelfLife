package com.shelflife.shelflife.controller;

import com.shelflife.shelflife.model.FoodItem;
import com.shelflife.shelflife.model.User;
import com.shelflife.shelflife.service.FoodItemService;
import com.shelflife.shelflife.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDate;

@Controller
public class DashboardController {

    @Autowired
    private FoodItemService foodItemService;

    @Autowired
    private UserService userService;


    @GetMapping("/dashboard")
    public String showDashboard(Model model, @AuthenticationPrincipal UserDetails currentUser) {
        if (currentUser != null) {
            User user = userService.findByEmail(currentUser.getUsername());
            if (user != null) {
                model.addAttribute("foodItems", foodItemService.getAllItemsByUser(user));
                model.addAttribute("newItem", new FoodItem());
                return "dashboard";
            }
        }
        return "redirect:/login";
    }

    @PostMapping("/dashboard/add")
            public String addFoodItem(@ModelAttribute("newItem") FoodItem item,
                                      @AuthenticationPrincipal UserDetails currentUser){

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
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard/delete/{id}")
    public String deleteFoodItem(@PathVariable Long id){
        foodItemService.deleteItem(id);
        return "redirect:/dashboard";
    }
}
