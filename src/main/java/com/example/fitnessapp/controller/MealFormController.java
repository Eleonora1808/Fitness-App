package com.example.fitnessapp.controller;

import com.example.fitnessapp.entities.DailyLog;
import com.example.fitnessapp.entities.Meal;
import com.example.fitnessapp.repository.DailyLogRepository;
import com.example.fitnessapp.repository.MealRepository;
import com.example.fitnessapp.repository.UserRepository;
import com.example.fitnessapp.service.DailyLogService;
import com.example.fitnessapp.service.MealService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/meals")
public class MealFormController {

    private final MealService mealService;
    private final DailyLogRepository dailyLogRepository;
    private final UserRepository userRepository;
    private final DailyLogService dailyLogService;
    private final MealRepository mealRepository;

    public MealFormController(
        MealService mealService,
        DailyLogRepository dailyLogRepository,
        UserRepository userRepository,
        DailyLogService dailyLogService,
        MealRepository mealRepository
    ) {
        this.mealService = mealService;
        this.dailyLogRepository = dailyLogRepository;
        this.userRepository = userRepository;
        this.dailyLogService = dailyLogService;
        this.mealRepository = mealRepository;
    }

    @GetMapping
    public String listMeals(Principal principal, Model model) {
        UUID userId = getUserId(principal);
        List<Meal> meals = mealRepository.findByUserId(userId);
        model.addAttribute("meals", meals);
        return "meals/list";
    }

    @GetMapping("/new")
    public String showAddMealForm(
        Principal principal,
        @RequestParam(required = false) String date,
        Model model
    ) {
        UUID userId = getUserId(principal);
        LocalDate mealDate = date != null ? LocalDate.parse(date) : LocalDate.now();
        
        DailyLog log = dailyLogRepository.findByUserAndDate(
            userRepository.findById(userId).orElseThrow(), 
            mealDate
        );
        if (log == null) {
            log = dailyLogService.createDailyLog(userId, mealDate, null);
        }
        
        model.addAttribute("meal", new Meal());
        model.addAttribute("dailyLogId", log.getId());
        model.addAttribute("date", mealDate);
        return "meals/new";
    }

    @PostMapping
    public String addMeal(
        @RequestParam UUID dailyLogId,
        @Valid @ModelAttribute("meal") Meal meal,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.meal", bindingResult);
            redirectAttributes.addFlashAttribute("meal", meal);
            redirectAttributes.addFlashAttribute("errorMessage", "Please fill in all required fields correctly");
            DailyLog log = dailyLogRepository.findById(dailyLogId).orElseThrow();
            return "redirect:/meals/new?date=" + log.getDate().format(DateTimeFormatter.ISO_DATE);
        }

        try {
            mealService.addMeal(dailyLogId, meal);
            redirectAttributes.addFlashAttribute("successMessage", "Meal added successfully");
            return "redirect:/logs";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to save meal: " + e.getMessage());
            DailyLog log = dailyLogRepository.findById(dailyLogId).orElseThrow();
            return "redirect:/meals/new?date=" + log.getDate().format(DateTimeFormatter.ISO_DATE);
        }
    }

    private UUID getUserId(Principal principal) {
        String username = principal.getName();
        return userRepository
            .findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"))
            .getId();
    }
}

