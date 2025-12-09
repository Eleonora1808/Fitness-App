package com.example.fitnessapp.service;

import com.example.fitnessapp.dto.FoodCalculationResponse;
import com.example.fitnessapp.entities.DailyLog;
import com.example.fitnessapp.entities.Meal;
import com.example.fitnessapp.repository.DailyLogRepository;
import com.example.fitnessapp.repository.MealRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MealService {

    private final MealRepository mealRepository;
    private final DailyLogRepository dailyLogRepository;
    private final DailyLogService dailyLogService;
    private final FoodService foodService;

    public MealService(
        MealRepository mealRepository,
        DailyLogRepository dailyLogRepository,
        DailyLogService dailyLogService,
        FoodService foodService
    ) {
        this.mealRepository = mealRepository;
        this.dailyLogRepository = dailyLogRepository;
        this.dailyLogService = dailyLogService;
        this.foodService = foodService;
    }

    @Transactional
    public Meal addMeal(UUID dailyLogId, Meal meal) {
        DailyLog log = requireDailyLog(dailyLogId);
        meal.setDailyLog(log);

        if (meal.getFoodName() == null || meal.getFoodName().trim().isEmpty()) {
            throw new IllegalArgumentException("Food name is required");
        }
        if (meal.getServingSize() == null || meal.getServingSize().trim().isEmpty()) {
            throw new IllegalArgumentException("Serving size is required");
        }
        if (meal.getMealType() == null) {
            throw new IllegalArgumentException("Meal type is required");
        }
        
        if (meal.getCalories() == null && meal.getFoodName() != null && meal.getServingSize() != null) {
            try {
                FoodCalculationResponse calculation = foodService.calculateCalories(
                    meal.getFoodName(),
                    meal.getServingSize(),
                    1.0
                );
                
                if (calculation != null) {
                    meal.setCalories(calculation.calories());
                    meal.setProtein(calculation.protein());
                    meal.setCarbs(calculation.carbs());
                    meal.setFats(calculation.fats());
                }
            } catch (Exception e) {
            }
        }
        
        Meal saved = mealRepository.save(meal);
        recalculate(log);
        return saved;
    }

    @Transactional
    public Meal updateMeal(UUID mealId, Meal updates) {
        Meal meal = requireMeal(mealId);
        if (updates.getMealType() != null) {
            meal.setMealType(updates.getMealType());
        }
        if (updates.getFoodName() != null) {
            meal.setFoodName(updates.getFoodName());
        }
        if (updates.getServingSize() != null) {
            meal.setServingSize(updates.getServingSize());
        }
        if (updates.getCalories() != null) {
            meal.setCalories(updates.getCalories());
        }
        if (updates.getProtein() != null) {
            meal.setProtein(updates.getProtein());
        }
        if (updates.getCarbs() != null) {
            meal.setCarbs(updates.getCarbs());
        }
        if (updates.getFats() != null) {
            meal.setFats(updates.getFats());
        }
        Meal saved = mealRepository.save(meal);
        recalculate(meal.getDailyLog());
        return saved;
    }

    @Transactional
    public void deleteMeal(UUID mealId) {
        Meal meal = requireMeal(mealId);
        DailyLog log = meal.getDailyLog();
        mealRepository.delete(meal);
        recalculate(log);
    }

    @Transactional(readOnly = true)
    public List<Meal> listMeals(UUID dailyLogId) {
        return mealRepository.findByDailyLogId(dailyLogId);
    }

    private Meal requireMeal(UUID mealId) {
        return mealRepository.findById(mealId).orElseThrow(() -> new EntityNotFoundException("Meal not found"));
    }

    private DailyLog requireDailyLog(UUID dailyLogId) {
        return dailyLogRepository
            .findById(dailyLogId)
            .orElseThrow(() -> new EntityNotFoundException("Daily log not found"));
    }

    private void recalculate(DailyLog log) {
        if (log == null) {
            return;
        }
        dailyLogService.computeDailyTotals(log.getUser().getId(), log.getDate());
    }
}


