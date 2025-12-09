package com.example.fitnessapp.client;

import com.example.fitnessapp.dto.FoodDto;
import com.example.fitnessapp.dto.MicroserviceFoodPageResponse;
import com.example.fitnessapp.dto.NutritionCalculationRequest;
import com.example.fitnessapp.dto.NutritionCalculationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "food-microservice", url = "${fitnessapp.microservice.food.url}")
public interface FoodMicroserviceClient {

    @PostMapping("/api/foods/calc")
    NutritionCalculationResponse calculateCalories(@RequestBody NutritionCalculationRequest request);

    @GetMapping("/api/foods/search")
    MicroserviceFoodPageResponse searchFoods(@RequestParam("name") String name);

    @GetMapping("/api/foods/{id}")
    FoodDto getFoodById(@PathVariable Long id);

    @PostMapping("/api/foods")
    FoodDto createFood(@RequestBody FoodDto food);

    @PutMapping("/api/foods/{id}")
    FoodDto updateFood(@PathVariable Long id, @RequestBody FoodDto food);
}

