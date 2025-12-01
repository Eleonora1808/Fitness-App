package com.example.fitnessapp.controller;

import com.example.fitnessapp.entities.Role;
import com.example.fitnessapp.service.AdminService;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/users/{id}/block")
    public ResponseEntity<Void> blockUser(@PathVariable UUID id) {
        adminService.blockUser(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/{id}/roles")
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

