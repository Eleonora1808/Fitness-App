package com.example.fitnessapp.controller;

import com.example.fitnessapp.entities.Role;
import com.example.fitnessapp.entities.User;
import com.example.fitnessapp.service.AdminService;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

     @GetMapping("/users")
    public String listUsers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        Model model
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = adminService.listUsers(pageable);
        model.addAttribute("users", users);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", users.getTotalPages());
        return "admin/users";
    }

    @PostMapping("/users/{id}/block")
    @org.springframework.web.bind.annotation.ResponseBody
    public ResponseEntity<Void> blockUser(@PathVariable UUID id) {
        adminService.blockUser(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/{id}/unblock")
    @org.springframework.web.bind.annotation.ResponseBody
    public ResponseEntity<Void> unblockUser(@PathVariable UUID id) {
        adminService.unblockUser(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/{id}/roles")
    @org.springframework.web.bind.annotation.ResponseBody
    public ResponseEntity<Void> assignRole(
        @PathVariable UUID id,
        @RequestParam Role role,
        @RequestParam(defaultValue = "assign") String action
    ) {
        if ("assign".equalsIgnoreCase(action)) {
            adminService.assignRole(id, role);
        } else if ("remove".equalsIgnoreCase(action)) {
            adminService.revokeRole(id, role);
        } else {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }
}

