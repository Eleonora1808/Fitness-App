package com.example.fitnessapp.service;

import com.example.fitnessapp.entities.DailyLog;
import com.example.fitnessapp.entities.Meal;
import com.example.fitnessapp.entities.User;
import com.example.fitnessapp.repository.DailyLogRepository;
import com.example.fitnessapp.repository.MealRepository;
import com.example.fitnessapp.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DailyLogService {

    private final DailyLogRepository dailyLogRepository;
    private final UserRepository userRepository;
    private final MealRepository mealRepository;

    public DailyLogService(
        DailyLogRepository dailyLogRepository,
        UserRepository userRepository,
        MealRepository mealRepository
    ) {
        this.dailyLogRepository = dailyLogRepository;
        this.userRepository = userRepository;
        this.mealRepository = mealRepository;
    }

    @Transactional
    public DailyLog createDailyLog(UUID userId, LocalDate date, String notes) {
        User user = requireUser(userId);
        DailyLog existing = dailyLogRepository.findByUserAndDate(user, date);
        if (existing != null) {
            return existing;
        }
        DailyLog log = new DailyLog();
        log.setUser(user);
        log.setDate(date);
        log.setNotes(notes);
        log.setTotalCaloriesIn(0);
        return dailyLogRepository.save(log);
    }

    @Transactional
    public DailyLog updateDailyLog(UUID dailyLogId, String notes) {
        DailyLog log = requireDailyLog(dailyLogId);
        log.setNotes(notes);
        return dailyLogRepository.save(log);
    }

    @Transactional
    public void deleteDailyLog(UUID dailyLogId) {
        DailyLog log = requireDailyLog(dailyLogId);
        dailyLogRepository.delete(log);
    }

    @Transactional
    public DailyLog computeDailyTotals(UUID userId, LocalDate date) {
        User user = requireUser(userId);
        DailyLog log = dailyLogRepository.findByUserAndDate(user, date);
        if (log == null) {
            throw new EntityNotFoundException("Daily log not found for date");
        }
        int caloriesIn = mealRepository
            .findByDailyLogId(log.getId())
            .stream()
            .map(Meal::getCalories)
            .filter(java.util.Objects::nonNull)
            .mapToInt(Integer::intValue)
            .sum();

        log.setTotalCaloriesIn(caloriesIn);
        return dailyLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public List<DailyLog> getLogsBetween(UUID userId, LocalDate start, LocalDate end) {
        User user = requireUser(userId);
        return dailyLogRepository.findByUserAndDateBetween(user, start, end);
    }

    private User requireUser(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    private DailyLog requireDailyLog(UUID dailyLogId) {
        return dailyLogRepository.findById(dailyLogId).orElseThrow(() -> new EntityNotFoundException("Daily log not found"));
    }
}


