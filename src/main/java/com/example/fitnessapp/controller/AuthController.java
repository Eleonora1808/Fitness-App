package com.example.fitnessapp.controller;

import com.example.fitnessapp.entities.User;
import com.example.fitnessapp.repository.UserRepository;
import com.example.fitnessapp.service.UserService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;
    private final UserRepository userRepository;

    public AuthController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new User());
        }
        return "auth/register";
    }

    @PostMapping("/register")
    public String processRegistration(
        @Valid @ModelAttribute("user") User user,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.user", bindingResult);
            redirectAttributes.addFlashAttribute("user", user);
            return "redirect:/register";
        }

        try {
            userService.register(user);
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! Please login.");
            return "redirect:/login";
        } catch (DuplicateKeyException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Username or email already taken");
            redirectAttributes.addFlashAttribute("user", user);
            return "redirect:/register";
        }
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "auth/login";
    }

    @GetMapping("/profile")
    public String showProfile(Principal principal, Model model) {
        User user = userService.requireUser(getUserId(principal));
        model.addAttribute("user", user);
        return "auth/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(
        @Valid @ModelAttribute("user") User updatedData,
        BindingResult bindingResult,
        Principal principal,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.user", bindingResult);
            redirectAttributes.addFlashAttribute("user", updatedData);
            return "redirect:/profile";
        }

        UUID userId = getUserId(principal);
        userService.updateProfile(userId, updatedData);
        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully");
        return "redirect:/profile";
    }

    private UUID getUserId(Principal principal) {
        String username = principal.getName();
        return userRepository
            .findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"))
            .getId();
    }
}

