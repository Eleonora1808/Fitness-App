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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

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
        logger.info("Assigning role {} to user ID: {}", role, userId);
        User user = userService.requireUser(userId);
        user.getRoles().add(role);
        User saved = userRepository.save(user);
        logger.info("Role assigned successfully");
        return saved;
    }

    @Transactional
    public User revokeRole(UUID userId, Role role) {
        logger.info("Revoking role {} from user ID: {}", role, userId);
        User user = userService.requireUser(userId);
        user.getRoles().remove(role);
        User saved = userRepository.save(user);
        logger.info("Role revoked successfully");
        return saved;
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
        logger.info("Deleting user ID: {}", userId);
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));
        userRepository.delete(user);
        logger.info("User deleted successfully: {}", userId);
    }
}


