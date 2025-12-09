package com.example.fitnessapp.dto;

import java.util.List;

public record FoodSearchResponse(
    List<FoodSearchItem> foods
) {
    public record FoodSearchItem(
        String id,
        String name,
        String servingSize,
        Integer calories
    ) {}
}

