package com.example.fitnessapp.controller;

import com.example.fitnessapp.entities.DailyLog;
import com.example.fitnessapp.entities.Workout;
import com.example.fitnessapp.repository.DailyLogRepository;
import com.example.fitnessapp.repository.UserRepository;
import com.example.fitnessapp.repository.WorkoutRepository;
import com.example.fitnessapp.service.DailyLogService;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final DailyLogService dailyLogService;
    private final UserRepository userRepository;
    private final DailyLogRepository dailyLogRepository;
    private final WorkoutRepository workoutRepository;

    public DashboardController(
        DailyLogService dailyLogService,
        UserRepository userRepository,
        DailyLogRepository dailyLogRepository,
        WorkoutRepository workoutRepository
    ) {
        this.dailyLogService = dailyLogService;
        this.userRepository = userRepository;
        this.dailyLogRepository = dailyLogRepository;
        this.workoutRepository = workoutRepository;
    }

    @GetMapping
    public String showDashboard(Principal principal, Model model) {
        UUID userId = getUserId(principal);
        LocalDate today = LocalDate.now();
        
        var user = userRepository.findById(userId).orElseThrow();
        DailyLog todayLog = dailyLogRepository.findByUserAndDate(user, today);
        
        if (todayLog != null) {
            dailyLogService.computeDailyTotals(userId, today);
            todayLog = dailyLogRepository.findByUserAndDate(user, today);
        }
        
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        LocalDateTime now = LocalDateTime.now();
        List<Workout> recentWorkouts = workoutRepository.findByUserAndDateTimeBetween(
            user, weekAgo, now
        );
        recentWorkouts = recentWorkouts.stream()
            .sorted((a, b) -> b.getDateTime().compareTo(a.getDateTime()))
            .limit(5)
            .toList();
        
        model.addAttribute("todayLog", todayLog);
        model.addAttribute("recentWorkouts", recentWorkouts);
        model.addAttribute("today", today);
        
        return "dashboard/index";
    }

    private UUID getUserId(Principal principal) {
        String username = principal.getName();
        return userRepository
            .findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"))
            .getId();
    }
}

