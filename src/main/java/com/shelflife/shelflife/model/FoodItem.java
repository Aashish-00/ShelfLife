package com.shelflife.shelflife.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "food_items")
@Data
@NoArgsConstructor
public class FoodItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String category;
    private Integer quantity;
    private LocalDate purchaseDate;
    private LocalDate expiryDate;
    private String notes;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
