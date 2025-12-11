package com.example.fitnessapp.service;

import com.example.fitnessapp.entities.DailyLog;
import com.example.fitnessapp.entities.Progress;
import com.example.fitnessapp.entities.User;
import com.example.fitnessapp.repository.ProgressRepository;
import com.example.fitnessapp.repository.UserRepository;
import com.example.fitnessapp.repository.WorkoutRepository;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;

@Service
public class ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    private final DailyLogService dailyLogService;
    private final ProgressRepository progressRepository;
    private final WorkoutRepository workoutRepository;
    private final UserRepository userRepository;

    public ReportService(
        DailyLogService dailyLogService,
        ProgressRepository progressRepository,
        WorkoutRepository workoutRepository,
        UserRepository userRepository
    ) {
        this.dailyLogService = dailyLogService;
        this.progressRepository = progressRepository;
        this.workoutRepository = workoutRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserReportSummary generateWeeklySummary(UUID userId, LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(6);
        return generateSummary(userId, weekStart, weekEnd);
    }

    @Transactional(readOnly = true)
    public UserReportSummary generateMonthlySummary(UUID userId, LocalDate month) {
        LocalDate start = month.withDayOfMonth(1);
        LocalDate end = start.plusMonths(1).minusDays(1);
        return generateSummary(userId, start, end);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "reportCache", key = "#userId.toString() + '_' + #start.toString() + '_' + #end.toString()")
    public UserReportSummary generateSummary(UUID userId, LocalDate start, LocalDate end) {
        logger.info("Generating report summary for user ID: {}, from {} to {}", userId, start, end);
        User user = requireUser(userId);
        List<DailyLog> logs = dailyLogService.getLogsBetween(userId, start, end);
        int caloriesIn = logs
            .stream()
            .map(DailyLog::getTotalCaloriesIn)
            .filter(java.util.Objects::nonNull)
            .mapToInt(Integer::intValue)
            .sum();
        int caloriesOut = logs
            .stream()
            .map(DailyLog::getTotalCaloriesOut)
            .filter(java.util.Objects::nonNull)
            .mapToInt(Integer::intValue)
            .sum();
        long workoutCount = workoutRepository.findByUserAndDateBetween(user, start, end).size();
        List<Progress> progress = progressRepository.findByUserAndDateBetween(user, start, end);
        progress.sort(Comparator.comparing(Progress::getDate));
        BigDecimal weightChange = BigDecimal.ZERO;
        if (!progress.isEmpty()) {
            BigDecimal startWeight = progress.get(0).getWeightKg() != null ? progress.get(0).getWeightKg() : BigDecimal.ZERO;
            BigDecimal endWeight =
                progress.get(progress.size() - 1).getWeightKg() != null ? progress.get(progress.size() - 1).getWeightKg() : BigDecimal.ZERO;
            weightChange = endWeight.subtract(startWeight);
        }
        List<DailyLog> logsWithNotes = logs.stream()
            .filter(log -> log.getNotes() != null && !log.getNotes().trim().isEmpty())
            .sorted(Comparator.comparing(DailyLog::getDate))
            .toList();
        UserReportSummary summary = new UserReportSummary(start, end, caloriesIn, caloriesOut, workoutCount, weightChange, logsWithNotes);
        logger.info("Report summary generated: {} calories in, {} calories out, {} workouts", caloriesIn, caloriesOut, workoutCount);
        return summary;
    }

    private User requireUser(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    public record UserReportSummary(
        LocalDate startDate,
        LocalDate endDate,
        int totalCaloriesIn,
        int totalCaloriesOut,
        long entriesCount,
        BigDecimal weightChange,
        List<DailyLog> dailyLogsWithNotes
    ) {}
}


