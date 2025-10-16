package com.shelflife.shelflife.service;

import com.shelflife.shelflife.model.FoodItem;
import com.shelflife.shelflife.model.User;
import com.shelflife.shelflife.repository.FoodItemRepository;
import com.shelflife.shelflife.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FoodItemService {

    @Autowired
    private  FoodItemRepository foodItemRepository;

    public List<FoodItem> getAllItemsByUser(User user){
        return foodItemRepository.findByUser(user);
    }

    public void addItem(FoodItem item){
        foodItemRepository.save(item);
    }

    public void deleteItem(Long id){
        foodItemRepository.deleteById(id);
    }
}
