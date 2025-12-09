package com.example.fitnessapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.UUID;

public record NutritionCalculationResponse(
    @JsonProperty("foodItemId") UUID foodItemId,
    @JsonProperty("foodName") String foodName,
    @JsonProperty("portionInGrams") BigDecimal portionInGrams,
    @JsonProperty("totals") NutritionTotals totals
) {
    public record NutritionTotals(
        @JsonProperty("calories") Integer calories,
        @JsonProperty("protein") BigDecimal protein,
        @JsonProperty("carbs") BigDecimal carbs,
        @JsonProperty("fat") BigDecimal fat,  // Note: "fat" (singular) to match API response
        @JsonProperty("fiber") BigDecimal fiber
    ) {}
}

