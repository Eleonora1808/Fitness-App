package com.example.fitnessapp.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record NutritionCalculationRequest(
    UUID foodItemId,
    BigDecimal portionInGrams
) {}

