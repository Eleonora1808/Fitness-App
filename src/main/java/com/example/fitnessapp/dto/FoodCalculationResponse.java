package com.example.fitnessapp.dto;

public record FoodCalculationResponse(
    String foodName,
    String servingSize,
    Double portions,
    Integer calories,
    Float protein,
    Float carbs,
    Float fats
) {}

