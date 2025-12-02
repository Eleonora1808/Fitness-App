package com.example.fitnessapp.controller;

import com.example.fitnessapp.service.UserService;
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

    public AdminReportController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public String showAdminUserReports(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        Model model
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<com.example.fitnessapp.entities.User> users = userService.listUsers(pageable);
        model.addAttribute("users", users);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", users.getTotalPages());
        return "admin/reports/users";
    }
}

