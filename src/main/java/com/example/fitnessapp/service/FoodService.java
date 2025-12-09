package com.example.fitnessapp.service;

import com.example.fitnessapp.client.FoodMicroserviceClient;
import com.example.fitnessapp.dto.FoodCalculationResponse;
import com.example.fitnessapp.dto.FoodDto;
import com.example.fitnessapp.dto.FoodSearchResponse;
import com.example.fitnessapp.dto.MicroserviceFoodPageResponse;
import com.example.fitnessapp.dto.NutritionCalculationRequest;
import com.example.fitnessapp.dto.NutritionCalculationResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class FoodService {

    private final FoodMicroserviceClient foodClient;

    public FoodService(FoodMicroserviceClient foodClient) {
        this.foodClient = foodClient;
    }

    public FoodCalculationResponse calculateCalories(String foodName, String servingSize, Double portions) {
        try {
            MicroserviceFoodPageResponse searchResponse = foodClient.searchFoods(foodName);
            if (searchResponse == null || searchResponse.content() == null || searchResponse.content().isEmpty()) {
                throw new RuntimeException("Food not found: " + foodName);
            }
            
            MicroserviceFoodPageResponse.MicroserviceFoodItem foodItem = searchResponse.content().stream()
                .filter(item -> item.name().equalsIgnoreCase(foodName))
                .findFirst()
                .orElse(searchResponse.content().get(0));
            
            UUID foodItemId = UUID.fromString(foodItem.id());
            
            BigDecimal portionInGrams = parseServingSizeToGrams(servingSize);
            
            if (portions != null && portions > 0) {
                portionInGrams = portionInGrams.multiply(BigDecimal.valueOf(portions));
            }
            
            NutritionCalculationRequest request = new NutritionCalculationRequest(foodItemId, portionInGrams);
            
            NutritionCalculationResponse microserviceResponse = foodClient.calculateCalories(request);
            
            NutritionCalculationResponse.NutritionTotals totals = microserviceResponse.totals();
            return new FoodCalculationResponse(
                foodName,
                servingSize,
                portions,
                totals.calories(),
                totals.protein() != null ? totals.protein().floatValue() : null,
                totals.carbs() != null ? totals.carbs().floatValue() : null,
                totals.fat() != null ? totals.fat().floatValue() : null
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate calories from microservice: " + e.getMessage(), e);
        }
    }
    
    private BigDecimal parseServingSizeToGrams(String servingSize) {
        if (servingSize == null || servingSize.trim().isEmpty()) {
            return BigDecimal.valueOf(100);
        }
        
        Pattern pattern = Pattern.compile("(\\d+(?:\\.\\d+)?)");
        Matcher matcher = pattern.matcher(servingSize);
        
        if (matcher.find()) {
            try {
                double value = Double.parseDouble(matcher.group(1));
                String lower = servingSize.toLowerCase();
                if (lower.contains("g") || lower.contains("gram")) {
                    return BigDecimal.valueOf(value);
                } else {
                    return BigDecimal.valueOf(value * 100);
                }
            } catch (NumberFormatException e) {
            }
        }
        
        return BigDecimal.valueOf(100);
    }

    public List<FoodSearchResponse.FoodSearchItem> searchFoods(String name) {
        try {
            MicroserviceFoodPageResponse response = foodClient.searchFoods(name);
            
            if (response == null || response.content() == null || response.content().isEmpty()) {
                return List.of();
            }
            
            List<FoodSearchResponse.FoodSearchItem> foods = response.content().stream()
                .map(item -> {
                    String servingSize = item.servingSizeGrams() != null 
                        ? item.servingSizeGrams() + "g" 
                        : "100g";
                    
                    Integer calories = item.caloriesPerServing();
                    
                    return new FoodSearchResponse.FoodSearchItem(
                        item.id(),
                        item.name(),
                        servingSize,
                        calories
                    );
                })
                .collect(Collectors.toList());
            
            return foods;
        } catch (Exception e) {
            return List.of();
        }
    }

    public FoodDto getFoodById(Long id) {
        try {
            return foodClient.getFoodById(id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get food from microservice: " + e.getMessage(), e);
        }
    }

    public FoodDto createFood(FoodDto food) {
        try {
            return foodClient.createFood(food);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create food in microservice: " + e.getMessage(), e);
        }
    }

    public FoodDto updateFood(Long id, FoodDto food) {
        try {
            return foodClient.updateFood(id, food);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update food in microservice: " + e.getMessage(), e);
        }
    }
}

