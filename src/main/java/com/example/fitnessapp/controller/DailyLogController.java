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
        Model model
    ) {
        UUID userId = getUserId(principal);
        LocalDate today = LocalDate.now();
        
        List<DailyLog> allLogs = dailyLogService.getLogsBetween(
            userId,
            LocalDate.MIN,
            LocalDate.MAX
        );
        
        ArrayList<DailyLog> logsWithNotes = new ArrayList<>();
        for (DailyLog log : allLogs) {
            if (log.getNotes() != null && !log.getNotes().trim().isEmpty()) {
                DailyLog refreshed = dailyLogRepository.findById(log.getId()).orElse(null);
                if (refreshed != null && refreshed.getNotes() != null && !refreshed.getNotes().trim().isEmpty()) {
                    logsWithNotes.add(refreshed);
                }
            }
        }
        logsWithNotes.sort((a, b) -> b.getDate().compareTo(a.getDate()));
        
        var user = userRepository.findById(userId).orElseThrow();
        DailyLog todayLog = dailyLogRepository.findByUserAndDate(user, today);
        if (todayLog == null) {
            todayLog = dailyLogService.createDailyLog(userId, today, null);
        }
        
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
        var user = userRepository.findById(userId).orElseThrow();
        DailyLog log = dailyLogRepository.findByUserAndDate(user, localDate);
        
        if (log == null) {
            return "redirect:/logs/new?date=" + date;
        }
        
        log = dailyLogRepository.findById(log.getId()).orElseThrow();
        model.addAttribute("log", log);
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
        Model model
    ) {
        DailyLog log = new DailyLog();
        if (date != null) {
            try {
                log.setDate(LocalDate.parse(date, DateTimeFormatter.ISO_DATE));
            } catch (Exception e) {
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

