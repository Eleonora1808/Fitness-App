package com.example.fitnessapp.service;

import com.example.fitnessapp.entities.DailyLog;
import com.example.fitnessapp.entities.Meal;
import com.example.fitnessapp.entities.User;
import com.example.fitnessapp.entities.Workout;
import com.example.fitnessapp.repository.DailyLogRepository;
import com.example.fitnessapp.repository.MealRepository;
import com.example.fitnessapp.repository.UserRepository;
import com.example.fitnessapp.repository.WorkoutRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DailyLogService {

    private static final Logger logger = LoggerFactory.getLogger(DailyLogService.class);

    private final DailyLogRepository dailyLogRepository;
    private final UserRepository userRepository;
    private final MealRepository mealRepository;
    private final WorkoutRepository workoutRepository;

    public DailyLogService(
        DailyLogRepository dailyLogRepository,
        UserRepository userRepository,
        MealRepository mealRepository,
        WorkoutRepository workoutRepository
    ) {
        this.dailyLogRepository = dailyLogRepository;
        this.userRepository = userRepository;
        this.mealRepository = mealRepository;
        this.workoutRepository = workoutRepository;
    }

    @Transactional
    public DailyLog createDailyLog(UUID userId, LocalDate date, String notes) {
        logger.info("Creating daily log for user ID: {}, date: {}", userId, date);
        User user = requireUser(userId);
        DailyLog existing = dailyLogRepository.findByUserAndDate(user, date);
        if (existing != null) {
            if (notes != null && !notes.trim().isEmpty()) {
                existing.setNotes(notes.trim());
                DailyLog saved = dailyLogRepository.save(existing);
                logger.info("Updated existing daily log with notes for user ID: {}", userId);
                return saved;
            }
            logger.debug("Daily log already exists for user ID: {}, date: {}", userId, date);
            return existing;
        }
        DailyLog log = new DailyLog();
        log.setUser(user);
        log.setDate(date);
        log.setNotes(notes != null && !notes.trim().isEmpty() ? notes.trim() : null);
        log.setTotalCaloriesIn(0);
        log.setTotalCaloriesOut(0);
        DailyLog saved = dailyLogRepository.save(log);
        dailyLogRepository.flush();
        logger.info("Daily log created successfully with ID: {}", saved.getId());
        return saved;
    }

    @Transactional
    public DailyLog updateDailyLog(UUID dailyLogId, String notes) {
        logger.info("Updating daily log ID: {}", dailyLogId);
        DailyLog log = requireDailyLog(dailyLogId);
        log.setNotes(notes != null && !notes.trim().isEmpty() ? notes.trim() : null);
        DailyLog saved = dailyLogRepository.save(log);
        dailyLogRepository.flush();
        logger.info("Daily log updated successfully: {}", dailyLogId);
        return saved;
    }

    @Transactional
    public void deleteDailyLog(UUID dailyLogId) {
        DailyLog log = requireDailyLog(dailyLogId);
        dailyLogRepository.delete(log);
    }

    @Transactional
    public DailyLog computeDailyTotals(UUID userId, LocalDate date) {
        logger.debug("Computing daily totals for user ID: {}, date: {}", userId, date);
        User user = requireUser(userId);
        DailyLog log = dailyLogRepository.findByUserAndDate(user, date);
        if (log == null) {
            logger.warn("Daily log not found for user ID: {}, date: {}", userId, date);
            throw new EntityNotFoundException("Daily log not found for date");
        }
        int caloriesIn = mealRepository
            .findByDailyLogId(log.getId())
            .stream()
            .map(Meal::getCalories)
            .filter(java.util.Objects::nonNull)
            .mapToInt(Integer::intValue)
            .sum();

        int caloriesOut = workoutRepository
            .findByUserAndDateBetween(user, date, date)
            .stream()
            .map(Workout::getCaloriesBurned)
            .filter(java.util.Objects::nonNull)
            .mapToInt(Integer::intValue)
            .sum();

        log.setTotalCaloriesIn(caloriesIn);
        log.setTotalCaloriesOut(caloriesOut);
        DailyLog saved = dailyLogRepository.save(log);
        logger.debug("Daily totals computed: calories in: {}, calories out: {}", caloriesIn, caloriesOut);
        return saved;
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

