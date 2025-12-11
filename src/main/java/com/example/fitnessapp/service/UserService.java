package com.example.fitnessapp.service;

import com.example.fitnessapp.entities.Role;
import com.example.fitnessapp.entities.User;
import com.example.fitnessapp.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class UserService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(User user) {
        logger.info("Registering new user with username: {}", user.getUsername());
        if (!userRepository.findByUsername(user.getUsername()).isEmpty()
            || !userRepository.findByEmail(user.getEmail()).isEmpty()) {
            logger.warn("Registration failed: username or email already taken for {}", user.getUsername());
            throw new DuplicateKeyException("Username or email already taken");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRoles().isEmpty()) {
            user.getRoles().add(Role.ROLE_USER);
        }
        User saved = userRepository.save(user);
        logger.info("User registered successfully with ID: {}", saved.getId());
        return saved;
    }

    @Transactional(readOnly = true)
    public User login(String username, String rawPassword) {
        logger.debug("Attempting login for user: {}", username);
        User user = userRepository
            .findByUsername(username)
            .orElseThrow(() -> {
                logger.warn("Login failed: user not found - {}", username);
                return new UsernameNotFoundException("User not found");
            });
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            logger.warn("Login failed: invalid credentials for user {}", username);
            throw new BadCredentialsException("Invalid credentials");
        }
        if (Boolean.FALSE.equals(user.getActive())) {
            logger.warn("Login failed: user is blocked - {}", username);
            throw new BadCredentialsException("User is blocked");
        }
        logger.info("User logged in successfully: {}", username);
        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository
            .findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        boolean enabled = Boolean.TRUE.equals(user.getActive());
        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            enabled,
            true,   
            true,    
            true,    
            user.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.name())).collect(Collectors.toSet())
        );
    }

    @Transactional
    public User updateProfile(UUID userId, User updatedData) {
        logger.info("Updating profile for user ID: {}", userId);
        User user = requireUser(userId);
        if (StringUtils.hasText(updatedData.getEmail())) {
            user.setEmail(updatedData.getEmail());
        }
        if (updatedData.getAge() != null) {
            user.setAge(updatedData.getAge());
        }
        if (updatedData.getGender() != null) {
            user.setGender(updatedData.getGender());
        }
        if (updatedData.getHeightCm() != null) {
            user.setHeightCm(updatedData.getHeightCm());
        }
        if (updatedData.getCurrentWeightKg() != null) {
            user.setCurrentWeightKg(updatedData.getCurrentWeightKg());
        }
        if (updatedData.getGoal() != null) {
            user.setGoal(updatedData.getGoal());
        }
        User saved = userRepository.save(user);
        logger.info("Profile updated successfully for user ID: {}", userId);
        return saved;
    }

    @Transactional
    public User updateProfileFields(UUID userId, Integer age, String currentWeightKgStr, String goalStr) {
        logger.info("Updating profile fields for user ID: {}, age: {}, weight: {}, goal: {}", userId, age, currentWeightKgStr, goalStr);
        User user = requireUser(userId);
        
        user.setAge(age);
        
        if (currentWeightKgStr != null && !currentWeightKgStr.trim().isEmpty()) {
            try {
                user.setCurrentWeightKg(new java.math.BigDecimal(currentWeightKgStr));
            } catch (NumberFormatException e) {
                logger.warn("Invalid weight format: {}", currentWeightKgStr);
                throw new IllegalArgumentException("Invalid weight format");
            }
        } else {
            user.setCurrentWeightKg(null);
        }
        
        if (goalStr != null && !goalStr.trim().isEmpty()) {
            try {
                user.setGoal(com.example.fitnessapp.entities.Goal.valueOf(goalStr));
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid goal value: {}", goalStr);
                throw new IllegalArgumentException("Invalid goal value");
            }
        } else {
            user.setGoal(null);
        }
        
        User saved = userRepository.save(user);
        logger.info("Profile fields updated successfully for user ID: {}", userId);
        return saved;
    }

    @Transactional
    public void changePassword(UUID userId, String currentPassword, String newPassword) {
        logger.info("Changing password for user ID: {}", userId);
        User user = requireUser(userId);
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            logger.warn("Password change failed: invalid current password for user ID: {}", userId);
            throw new BadCredentialsException("Current password invalid");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        logger.info("Password changed successfully for user ID: {}", userId);
    }

    @Transactional
    public User blockUser(UUID userId) {
        logger.info("Blocking user ID: {}", userId);
        User user = requireUser(userId);
        user.setActive(Boolean.FALSE);
        User saved = userRepository.save(user);
        logger.info("User blocked successfully: {}", userId);
        return saved;
    }

    @Transactional
    public User unblockUser(UUID userId) {
        logger.info("Unblocking user ID: {}", userId);
        User user = requireUser(userId);
        user.setActive(Boolean.TRUE);
        User saved = userRepository.save(user);
        logger.info("User unblocked successfully: {}", userId);
        return saved;
    }

    @Transactional(readOnly = true)
    public Page<User> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<User> searchUsers(String keyword, Pageable pageable) {
        if (!StringUtils.hasText(keyword)) {
            return listUsers(pageable);
        }
        return userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            keyword,
            keyword,
            pageable
        );
    }

    @Transactional(readOnly = true)
    public List<User> findByRole(Role role) {
        return userRepository.findByRoles(role);
    }

    @Transactional(readOnly = true)
    public User requireUser(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));
    }
}


