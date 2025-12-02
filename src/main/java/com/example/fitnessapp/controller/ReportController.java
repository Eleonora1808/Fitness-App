package com.example.fitnessapp.controller;

import com.example.fitnessapp.repository.UserRepository;
import com.example.fitnessapp.service.ReportService;
import java.security.Principal;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;
    private final UserRepository userRepository;

    public ReportController(
        ReportService reportService,
        UserRepository userRepository
    ) {
        this.reportService = reportService;
        this.userRepository = userRepository;
    }

    @GetMapping("/weekly")
    public String showWeeklyReport(
        Principal principal,
        @RequestParam(required = false) LocalDate weekStart,
        Model model
    ) {
        UUID userId = getUserId(principal);
        if (weekStart == null) {
            weekStart = LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue() - 1);
        }
        var summary = reportService.generateWeeklySummary(userId, weekStart);
        model.addAttribute("summary", summary);
        model.addAttribute("weekStart", weekStart);
        return "reports/weekly";
    }

    private UUID getUserId(Principal principal) {
        String username = principal.getName();
        return userRepository
            .findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"))
            .getId();
    }
}

