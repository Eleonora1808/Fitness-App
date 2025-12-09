package com.example.fitnessapp.controller;

import com.example.fitnessapp.dto.FoodCalculationRequest;
import com.example.fitnessapp.dto.FoodCalculationResponse;
import com.example.fitnessapp.dto.FoodDto;
import com.example.fitnessapp.dto.FoodSearchResponse;
import com.example.fitnessapp.service.FoodService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/foods")
public class FoodController {

    private final FoodService foodService;

    public FoodController(FoodService foodService) {
        this.foodService = foodService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<FoodSearchResponse.FoodSearchItem>> searchFoods(
        @RequestParam("name") String name
    ) {
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        
        List<FoodSearchResponse.FoodSearchItem> foods = foodService.searchFoods(name.trim());
        return ResponseEntity.ok(foods);
    }

    @GetMapping("/all")
    public ResponseEntity<List<FoodSearchResponse.FoodSearchItem>> getAllFoods() {
        List<FoodSearchResponse.FoodSearchItem> allFoods = new ArrayList<>();
        Set<String> seenIds = new HashSet<>();
        
        String[] commonLetters = {"a", "e", "i", "o", "u", "b", "c", "d", "f", "g", "h", "m", "p", "r", "s", "t"};
        for (String letter : commonLetters) {
            try {
                List<FoodSearchResponse.FoodSearchItem> foods = foodService.searchFoods(letter);
                for (FoodSearchResponse.FoodSearchItem food : foods) {
                    if (food.id() != null && !seenIds.contains(food.id())) {
                        allFoods.add(food);
                        seenIds.add(food.id());
                    }
                }
                if (allFoods.size() >= 100) {
                    break;
                }
            } catch (Exception e) {
            }
        }
        
        allFoods.sort((a, b) -> a.name().compareToIgnoreCase(b.name()));
        
        return ResponseEntity.ok(allFoods);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FoodDto> getFoodById(@PathVariable Long id) {
        try {
            FoodDto food = foodService.getFoodById(id);
            return ResponseEntity.ok(food);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/calc")
    public ResponseEntity<FoodCalculationResponse> calculateCalories(@RequestBody FoodCalculationRequest request) {
        try {
            if (request == null) {
                return ResponseEntity.badRequest().build();
            }
            
            if (request.foodName() == null || request.foodName().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            if (request.servingSize() == null || request.servingSize().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            FoodCalculationResponse response = foodService.calculateCalories(
                request.foodName(),
                request.servingSize(),
                request.portions()
            );
            
            if (response == null) {
                return ResponseEntity.badRequest().build();
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

