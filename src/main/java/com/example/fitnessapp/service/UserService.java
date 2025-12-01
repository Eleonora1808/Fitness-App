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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(User user) {
        if (!userRepository.findByUsername(user.getUsername()).isEmpty()
            || !userRepository.findByEmail(user.getEmail()).isEmpty()) {
            throw new DuplicateKeyException("Username or email already taken");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRoles().isEmpty()) {
            user.getRoles().add(Role.ROLE_USER);
        }
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User login(String username, String rawPassword) {
        User user = userRepository
            .findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        if (Boolean.FALSE.equals(user.getActive())) {
            throw new BadCredentialsException("User is blocked");
        }
        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository
            .findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            user.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.name())).collect(Collectors.toSet())
        );
    }

    @Transactional
    public User updateProfile(UUID userId, User updatedData) {
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
        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(UUID userId, String currentPassword, String newPassword) {
        User user = requireUser(userId);
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BadCredentialsException("Current password invalid");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public User blockUser(UUID userId) {
        User user = requireUser(userId);
        user.setActive(Boolean.FALSE);
        return userRepository.save(user);
    }

    @Transactional
    public User unblockUser(UUID userId) {
        User user = requireUser(userId);
        user.setActive(Boolean.TRUE);
        return userRepository.save(user);
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

