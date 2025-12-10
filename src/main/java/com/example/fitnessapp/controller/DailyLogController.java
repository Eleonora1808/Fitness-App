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
import java.util.ArrayList;
import java.util.List;
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
    private final MealRepository mealRepository;

    public DailyLogController(
        DailyLogService dailyLogService,
        MealService mealService,
        UserRepository userRepository,
        DailyLogRepository dailyLogRepository,
        MealRepository mealRepository
    ) {
        this.dailyLogService = dailyLogService;
        this.mealService = mealService;
        this.userRepository = userRepository;
        this.dailyLogRepository = dailyLogRepository;
        this.mealRepository = mealRepository;
    }

    @GetMapping
    public String listLogs(
        Principal principal,
        Model model
    ) {
        UUID userId = getUserId(principal);
        LocalDate today = LocalDate.now();
        
        List<DailyLog> allLogs = dailyLogService.getLogsBetween(
            userId,
            LocalDate.MIN,
            LocalDate.MAX
        );

        var user = userRepository.findById(userId).orElseThrow();
        DailyLog todayLog = dailyLogRepository.findByUserAndDate(user, today);
        if (todayLog == null) {
            todayLog = dailyLogService.createDailyLog(userId, today, null);
        }
        
        ArrayList<DailyLog> logsWithNotes = new ArrayList<>();
        for (DailyLog log : allLogs) {
            if (log.getDate().equals(today)) {
                continue;
            }
            if (log.getNotes() != null && !log.getNotes().trim().isEmpty()) {
                DailyLog refreshed = dailyLogRepository.findById(log.getId()).orElse(null);
                if (refreshed != null && refreshed.getNotes() != null && !refreshed.getNotes().trim().isEmpty()) {
                    logsWithNotes.add(refreshed);
                }
            }
        }
        logsWithNotes.sort((a, b) -> b.getDate().compareTo(a.getDate()));
        
        model.addAttribute("logsWithNotes", logsWithNotes);
        model.addAttribute("todayLog", todayLog);
        model.addAttribute("today", today);
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
        LocalDate today = LocalDate.now();
        var user = userRepository.findById(userId).orElseThrow();
        DailyLog log = dailyLogRepository.findByUserAndDate(user, localDate);
        
        if (log == null) {
            return "redirect:/logs/new?date=" + date;
        }
        
        log = dailyLogRepository.findById(log.getId()).orElseThrow();

        DailyLog todayLog = dailyLogRepository.findByUserAndDate(user, today);
        List<Meal> meals = new ArrayList<>();
        if (todayLog != null) {
            meals = mealRepository.findByDailyLogId(todayLog.getId());
        }

        model.addAttribute("log", log);
        model.addAttribute("meals", meals);
        model.addAttribute("today", today);
        return "logs/view";
    }

    @PostMapping("/{date}/notes")
    public String updateNotes(
        @PathVariable String date,
        @RequestParam String notes,
        Principal principal,
        RedirectAttributes redirectAttributes
    ) {
        UUID userId = getUserId(principal);
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
        var user = userRepository.findById(userId).orElseThrow();
        DailyLog log = dailyLogRepository.findByUserAndDate(user, localDate);
        
        if (log == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Daily log not found for this date");
            return "redirect:/logs";
        }
        
        String notesToSave = notes != null && !notes.trim().isEmpty() ? notes.trim() : null;
        dailyLogService.updateDailyLog(log.getId(), notesToSave);
        redirectAttributes.addFlashAttribute("successMessage", "Notes saved successfully");
        return "redirect:/logs";
    }

    @GetMapping("/new")
    public String showNewLogForm(
        @RequestParam(required = false) String date,
        Principal principal,
        Model model
    ) {
        LocalDate logDate;
        if (date != null) {
            try {
                logDate = LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
            } catch (Exception e) {
                logDate = LocalDate.now();
            }
        } else {
            logDate = LocalDate.now();
        }

        DailyLog log = null;
        if (principal != null) {
            UUID userId = getUserId(principal);
            var user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                log = dailyLogRepository.findByUserAndDate(user, logDate);
            }
        }
        
        if (log == null) {
            log = new DailyLog();
            log.setDate(logDate);
        }
        
        model.addAttribute("log", log);
        return "logs/new";
    }

    @PostMapping
    public String createLog(
        @Valid @ModelAttribute("log") DailyLog log,
        BindingResult bindingResult,
        Principal principal,
        RedirectAttributes redirectAttributes,
        Model model
    ) {
        if (bindingResult.hasErrors()) {
            return "logs/new";
        }

        try{
        UUID userId = getUserId(principal);
        String notes = log.getNotes() != null && !log.getNotes().trim().isEmpty() ? log.getNotes().trim() : null;
        dailyLogService.createDailyLog(userId, log.getDate(), notes);
        redirectAttributes.addFlashAttribute("successMessage", "Daily log created successfully");
        return "redirect:/logs";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to create log: " + e.getMessage());
            return "logs/new";
        }
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

