package com.example.fitnessapp.dto;

public record FoodDto(
    Long id,
    String name,
    String servingSize,
    Integer calories,
    Float protein,
    Float carbs,
    Float fats
) {}

