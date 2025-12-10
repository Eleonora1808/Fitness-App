package com.example.fitnessapp.repository;

import com.example.fitnessapp.entities.Meal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MealRepository extends JpaRepository<Meal, UUID> {

    List<Meal> findByDailyLogId(UUID dailyLogId);

    @Query("select m from Meal m where m.dailyLog.user.id = :userId and m.dailyLog.date = :date")
    List<Meal> findByUserIdWithDate(@Param("userId") UUID userId, @Param("date") LocalDate date);

    @Query("select m from Meal m where m.dailyLog.user.id = :userId order by m.dailyLog.date desc, m.mealType")
    List<Meal> findByUserId(@Param("userId") UUID userId);
}


