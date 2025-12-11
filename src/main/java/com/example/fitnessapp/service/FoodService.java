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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

@Service
public class FoodService {

    private static final Logger logger = LoggerFactory.getLogger(FoodService.class);

    private final FoodMicroserviceClient foodClient;

    public FoodService(FoodMicroserviceClient foodClient) {
        this.foodClient = foodClient;
    }

    public FoodCalculationResponse calculateCalories(String foodName, String servingSize, Double portions) {
        logger.info("Calculating calories for food: {}, serving size: {}, portions: {}", foodName, servingSize, portions);
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
            FoodCalculationResponse response = new FoodCalculationResponse(
                foodName,
                servingSize,
                portions,
                totals.calories(),
                totals.protein() != null ? totals.protein().floatValue() : null,
                totals.carbs() != null ? totals.carbs().floatValue() : null,
                totals.fat() != null ? totals.fat().floatValue() : null
            );
            logger.info("Calories calculated successfully: {} calories", response.calories());
            return response;
        } catch (Exception e) {
            logger.error("Failed to calculate calories from microservice for food: {}", foodName, e);
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

    @Cacheable(value = "foodSearchCache", key = "#name")
    public List<FoodSearchResponse.FoodSearchItem> searchFoods(String name) {
        logger.info("Searching foods with name: {}", name);
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
            
           logger.info("Found {} foods matching: {}", foods.size(), name);
            return foods;
        } catch (Exception e) {
            logger.warn("Error searching foods: {}", e.getMessage());
            return List.of();
        }
    }

    @Cacheable(value = "foodCache", key = "#id")
    public FoodDto getFoodById(Long id) {
        logger.info("Getting food by ID: {}", id);
        try {
            FoodDto food = foodClient.getFoodById(id);
            logger.info("Food retrieved successfully: {}", food.name());
            return food;
        } catch (Exception e) {
            logger.error("Failed to get food from microservice with ID: {}", id, e);
            throw new RuntimeException("Failed to get food from microservice: " + e.getMessage(), e);
        }
    }

     @CacheEvict(value = {"foodSearchCache", "foodCache"}, allEntries = true)
    public FoodDto createFood(FoodDto food) {
        logger.info("Creating food: {}", food.name());
        try {
            FoodDto created = foodClient.createFood(food);
            logger.info("Food created successfully with ID: {}", created.id());
            return created;
        } catch (Exception e) {
            logger.error("Failed to create food in microservice: {}", food.name(), e);
            throw new RuntimeException("Failed to create food in microservice: " + e.getMessage(), e);
        }
    }

     @CacheEvict(value = {"foodSearchCache", "foodCache"}, allEntries = true)
    public FoodDto updateFood(Long id, FoodDto food) {
        logger.info("Updating food ID: {}, name: {}", id, food.name());
        try {
            FoodDto updated = foodClient.updateFood(id, food);
            logger.info("Food updated successfully: {}", id);
            return updated;
        } catch (Exception e) {
            logger.error("Failed to update food in microservice ID: {}", id, e);
            throw new RuntimeException("Failed to update food in microservice: " + e.getMessage(), e);
        }
    }
}

