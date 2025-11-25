package com.example.fitnessapp.service;

import com.example.fitnessapp.entities.User;
import com.example.fitnessapp.entities.Workout;
import com.example.fitnessapp.entities.WorkoutType;
import com.example.fitnessapp.repository.UserRepository;
import com.example.fitnessapp.repository.WorkoutRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkoutService {

    private final WorkoutRepository workoutRepository;
    private final UserRepository userRepository;
    private final DailyLogService dailyLogService;

    public WorkoutService(
        WorkoutRepository workoutRepository,
        UserRepository userRepository,
        DailyLogService dailyLogService
    ) {
        this.workoutRepository = workoutRepository;
        this.userRepository = userRepository;
        this.dailyLogService = dailyLogService;
    }

    @Transactional
    public Workout addWorkout(UUID userId, Workout workout, boolean autoEstimateCalories) {
        User user = requireUser(userId);
        workout.setUser(user);
        if (autoEstimateCalories || workout.getCaloriesBurned() == null) {
            workout.setCaloriesBurned(estimateCaloriesBurned(user, workout));
        }
        Workout saved = workoutRepository.save(workout);
        recalculateLog(saved);
        return saved;
    }

    @Transactional
    public Workout updateWorkout(UUID workoutId, Workout updates, boolean autoEstimateCalories) {
        Workout workout = requireWorkout(workoutId);
        if (updates.getType() != null) {
            workout.setType(updates.getType());
        }
        if (updates.getDateTime() != null) {
            workout.setDateTime(updates.getDateTime());
        }
        if (updates.getDurationMinutes() != null) {
            workout.setDurationMinutes(updates.getDurationMinutes());
        }
        if (updates.getCaloriesBurned() != null) {
            workout.setCaloriesBurned(updates.getCaloriesBurned());
        } else if (autoEstimateCalories) {
            workout.setCaloriesBurned(estimateCaloriesBurned(workout.getUser(), workout));
        }
        if (updates.getNotes() != null) {
            workout.setNotes(updates.getNotes());
        }
        Workout saved = workoutRepository.save(workout);
        recalculateLog(saved);
        return saved;
    }

    @Transactional
    public void deleteWorkout(UUID workoutId) {
        Workout workout = requireWorkout(workoutId);
        LocalDate date = workout.getDateTime().toLocalDate();
        UUID userId = workout.getUser().getId();
        workoutRepository.delete(workout);
        dailyLogService.computeDailyTotals(userId, date);
    }

    @Transactional(readOnly = true)
    public List<Workout> findWorkouts(UUID userId, LocalDate start, LocalDate end) {
        User user = requireUser(userId);
        return workoutRepository.findByUserAndDateBetween(user, start, end);
    }

    private void recalculateLog(Workout workout) {
        LocalDate date = workout.getDateTime().toLocalDate();
        dailyLogService.computeDailyTotals(workout.getUser().getId(), date);
    }

    private Workout requireWorkout(UUID workoutId) {
        return workoutRepository.findById(workoutId).orElseThrow(() -> new EntityNotFoundException("Workout not found"));
    }

    private User requireUser(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    private int estimateCaloriesBurned(User user, Workout workout) {
        double weight = user.getCurrentWeightKg() != null ? user.getCurrentWeightKg().doubleValue() : 70.0;
        int duration = workout.getDurationMinutes() != null ? workout.getDurationMinutes() : 30;
        double met = switch (workout.getType() != null ? workout.getType() : WorkoutType.OTHER) {
            case CARDIO -> 8.0;
            case STRENGTH -> 6.0;
            case YOGA -> 3.0;
            default -> 4.0;
        };
        double calories = met * weight * duration / 60.0;
        return (int) Math.round(calories);
    }
}


