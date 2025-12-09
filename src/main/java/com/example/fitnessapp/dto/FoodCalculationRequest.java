package com.example.fitnessapp.dto;

public record FoodCalculationRequest(
    String foodName,
    String servingSize,
    Double portions
) {}

