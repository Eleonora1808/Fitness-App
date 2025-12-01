package com.example.fitnessapp.controller;

import com.example.fitnessapp.entities.DailyLog;
import com.example.fitnessapp.entities.Meal;
import com.example.fitnessapp.repository.DailyLogRepository;
import com.example.fitnessapp.repository.UserRepository;
import com.example.fitnessapp.service.DailyLogService;
import com.example.fitnessapp.service.MealService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/logs")
public class DailyLogController {

    private final DailyLogService dailyLogService;
    private final MealService mealService;
    private final UserRepository userRepository;
    private final DailyLogRepository dailyLogRepository;

    public DailyLogController(
        DailyLogService dailyLogService,
        MealService mealService,
        UserRepository userRepository,
        DailyLogRepository dailyLogRepository
    ) {
        this.dailyLogService = dailyLogService;
        this.mealService = mealService;
        this.userRepository = userRepository;
        this.dailyLogRepository = dailyLogRepository;
    }

    @GetMapping
    public String listLogs(
        Principal principal,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        Model model
    ) {
        UUID userId = getUserId(principal);
        
        var allLogs = dailyLogRepository.findByUserAndDateBetween(
            userRepository.findById(userId).orElseThrow(),
            LocalDate.MIN,
            LocalDate.MAX
        );
        
        int start = page * size;
        int end = Math.min(start + size, allLogs.size());
        var pagedLogs = allLogs.subList(start, end);
        
        model.addAttribute("logs", pagedLogs);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", (int) Math.ceil((double) allLogs.size() / size));
        model.addAttribute("totalItems", allLogs.size());
        return "logs/list";
    }

    @GetMapping("/{date}")
    public String viewLogByDate(
        @PathVariable String date,
        Principal principal,
        Model model
    ) {
        UUID userId = getUserId(principal);
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
        var user = userRepository.findById(userId).orElseThrow();
        DailyLog log = dailyLogRepository.findByUserAndDate(user, localDate);
        
        if (log == null) {
            return "redirect:/logs/new?date=" + date;
        }
        
        model.addAttribute("log", log);
        model.addAttribute("meals", mealService.listMeals(log.getId()));
        return "logs/view";
    }

    @GetMapping("/new")
    public String showNewLogForm(
        @RequestParam(required = false) String date,
        Model model
    ) {
        DailyLog log = new DailyLog();
        if (date != null) {
            try {
                log.setDate(LocalDate.parse(date, DateTimeFormatter.ISO_DATE));
            } catch (Exception e) {
                // Invalid date, use today
                log.setDate(LocalDate.now());
            }
        } else {
            log.setDate(LocalDate.now());
        }
        model.addAttribute("log", log);
        return "logs/new";
    }

    @PostMapping
    public String createLog(
        @Valid @ModelAttribute("log") DailyLog log,
        BindingResult bindingResult,
        Principal principal,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "logs/new";
        }

        UUID userId = getUserId(principal);
        dailyLogService.createDailyLog(userId, log.getDate(), log.getNotes());
        redirectAttributes.addFlashAttribute("successMessage", "Daily log created successfully");
        return "redirect:/logs";
    }

    @GetMapping("/{id}/meals/new")
    public String showAddMealForm(@PathVariable UUID id, Model model) {
        model.addAttribute("meal", new Meal());
        model.addAttribute("dailyLogId", id);
        return "meals/new";
    }

    @PostMapping("/{id}/meals")
    public String addMeal(
        @PathVariable UUID id,
        @Valid @ModelAttribute("meal") Meal meal,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "meals/new";
        }

        mealService.addMeal(id, meal);
        redirectAttributes.addFlashAttribute("successMessage", "Meal added successfully");
        return "redirect:/logs/" + dailyLogRepository.findById(id).orElseThrow().getDate();
    }

    private UUID getUserId(Principal principal) {
        String username = principal.getName();
        return userRepository
            .findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"))
            .getId();
    }
}

