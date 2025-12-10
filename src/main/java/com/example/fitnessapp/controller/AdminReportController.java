package com.example.fitnessapp.controller;

import com.example.fitnessapp.entities.Progress;
import com.example.fitnessapp.repository.ProgressRepository;
import com.example.fitnessapp.service.UserService;
import com.example.fitnessapp.entities.User;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/reports")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportController {

    private final UserService userService;
    private final ProgressRepository progressRepository;

    public AdminReportController(UserService userService, ProgressRepository progressRepository) {
        this.userService = userService;
        this.progressRepository = progressRepository;
    }

    @GetMapping("/users")
    public String showAdminUserReports(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        Model model
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = userService.listUsers(pageable);
        
        Map<UUID, BigDecimal> lastWeights = new HashMap<>();
        for (User user : users.getContent()) {
            progressRepository.findTopByUserOrderByDateDesc(user)
                .map(Progress::getWeightKg)
                .ifPresent(weight -> lastWeights.put(user.getId(), weight));
        }
        
        model.addAttribute("users", users);
        model.addAttribute("lastWeights", lastWeights);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", users.getTotalPages());
        return "admin/reports/users";
    }
}

