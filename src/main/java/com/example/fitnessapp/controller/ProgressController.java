package com.example.fitnessapp.controller;

import com.example.fitnessapp.entities.Progress;
import com.example.fitnessapp.repository.UserRepository;
import com.example.fitnessapp.service.ProgressService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/progress")
public class ProgressController {

    private final ProgressService progressService;
    private final UserRepository userRepository;

    public ProgressController(ProgressService progressService, UserRepository userRepository) {
        this.progressService = progressService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String showProgressChart(
        Principal principal,
        @RequestParam(required = false) LocalDate start,
        @RequestParam(required = false) LocalDate end,
        Model model
    ) {
        UUID userId = getUserId(principal);
        List<Progress> progressHistory = progressService.getProgressHistory(userId, start, end);
        var trend = progressService.computeTrend(userId, start, end);
        
        model.addAttribute("progressHistory", progressHistory);
        model.addAttribute("trend", trend);
        model.addAttribute("startDate", start);
        model.addAttribute("endDate", end);
        return "progress/chart";
    }

    @PostMapping
    public ResponseEntity<Progress> addProgress(
        Principal principal,
        @Valid @RequestBody ProgressRequest request
    ) {
        UUID userId = getUserId(principal);
        Progress progress = progressService.addProgress(
            userId,
            request.date() != null ? request.date() : LocalDate.now(),
            request.weightKg(),
            request.notes()
        );
        return ResponseEntity.ok(progress);
    }

    private UUID getUserId(Principal principal) {
        String username = principal.getName();
        return userRepository
            .findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"))
            .getId();
    }

    public record ProgressRequest(LocalDate date, BigDecimal weightKg, String notes) {}
}

