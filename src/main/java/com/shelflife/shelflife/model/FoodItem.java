package com.shelflife.shelflife.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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

    @Column(nullable = false)
    @NotBlank(message = "Food name is required")
    private String name;

    @Column(nullable = false)
    @NotBlank(message = "Category is required")
    private String category;

    @Positive(message = "Quantity must be positive")
    private Integer quantity = 1;

    private LocalDate purchaseDate;

    @Column(nullable = false)
    @NotNull(message = "Expiry date is required")
    private LocalDate expiryDate;

    private String notes;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}