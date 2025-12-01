package com.example.fitnessapp.controller;

import com.example.fitnessapp.entities.Meal;
import com.example.fitnessapp.service.MealService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/meals")
public class MealController {

    private final MealService mealService;

    public MealController(MealService mealService) {
        this.mealService = mealService;
    }

    @PutMapping("/{id}")
    public ResponseEntity<Meal> updateMeal(@PathVariable UUID id, @Valid @RequestBody Meal meal) {
        Meal updated = mealService.updateMeal(id, meal);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMeal(@PathVariable UUID id) {
        mealService.deleteMeal(id);
        return ResponseEntity.ok().build();
    }
}

