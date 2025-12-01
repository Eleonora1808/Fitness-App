package com.example.fitnessapp.service;

import com.example.fitnessapp.entities.Role;
import com.example.fitnessapp.entities.User;
import com.example.fitnessapp.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final UserService userService;

    public AdminService(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public Page<User> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Transactional
    public User assignRole(UUID userId, Role role) {
        User user = userService.requireUser(userId);
        user.getRoles().add(role);
        return userRepository.save(user);
    }

    @Transactional
    public User revokeRole(UUID userId, Role role) {
        User user = userService.requireUser(userId);
        user.getRoles().remove(role);
        return userRepository.save(user);
    }

    @Transactional
    public User blockUser(UUID userId) {
        return userService.blockUser(userId);
    }

    @Transactional
    public User unblockUser(UUID userId) {
        return userService.unblockUser(userId);
    }

    @Transactional
    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));
        userRepository.delete(user);
    }
}


