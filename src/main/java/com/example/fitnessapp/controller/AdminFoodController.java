package com.example.fitnessapp.controller;

import com.example.fitnessapp.dto.FoodDto;
import com.example.fitnessapp.service.FoodService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/foods")
@PreAuthorize("hasRole('ADMIN')")
public class AdminFoodController {

    private final FoodService foodService;

    public AdminFoodController(FoodService foodService) {
        this.foodService = foodService;
    }

    @PostMapping
    public ResponseEntity<FoodDto> createFood(@Valid @RequestBody FoodDto food) {
        try {
            FoodDto created = foodService.createFood(food);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<FoodDto> updateFood(
        @PathVariable Long id,
        @Valid @RequestBody FoodDto food
    ) {
        try {
            FoodDto updated = foodService.updateFood(id, food);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

