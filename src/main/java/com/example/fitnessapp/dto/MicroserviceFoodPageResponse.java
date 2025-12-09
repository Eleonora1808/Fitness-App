package com.example.fitnessapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record MicroserviceFoodPageResponse(
    @JsonProperty("content") List<MicroserviceFoodItem> content,
    @JsonProperty("totalElements") long totalElements,
    @JsonProperty("totalPages") int totalPages,
    @JsonProperty("number") int number,
    @JsonProperty("size") int size,
    @JsonProperty("first") boolean first,
    @JsonProperty("last") boolean last,
    @JsonProperty("empty") boolean empty,
    @JsonProperty("numberOfElements") int numberOfElements
) {
    public record MicroserviceFoodItem(
        @JsonProperty("id") String id,
        @JsonProperty("name") String name,
        @JsonProperty("description") String description,
        @JsonProperty("servingSizeGrams") Integer servingSizeGrams,
        @JsonProperty("caloriesPerServing") Integer caloriesPerServing,
        @JsonProperty("proteinPerServing") Double proteinPerServing,
        @JsonProperty("carbsPerServing") Double carbsPerServing,
        @JsonProperty("fatPerServing") Double fatPerServing,
        @JsonProperty("fiberPerServing") Double fiberPerServing,
        @JsonProperty("createdAt") String createdAt,
        @JsonProperty("updatedAt") String updatedAt
    ) {}
}

