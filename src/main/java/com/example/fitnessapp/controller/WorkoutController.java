package com.example.fitnessapp.controller;

import com.example.fitnessapp.entities.Workout;
import com.example.fitnessapp.repository.UserRepository;
import com.example.fitnessapp.service.WorkoutService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/workouts")
public class WorkoutController {

    private final WorkoutService workoutService;
    private final UserRepository userRepository;

    public WorkoutController(WorkoutService workoutService, UserRepository userRepository) {
        this.workoutService = workoutService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String listWorkouts(Principal principal, Model model) {
        UUID userId = getUserId(principal);
        List<Workout> workouts = workoutService.findWorkouts(userId, null, null);
        model.addAttribute("workouts", workouts);
        return "workouts/list";
    }

    @GetMapping("/new")
    public String showNewWorkoutForm(
        @RequestParam(required = false) String date,
        Model model
    ) {
        Workout workout = new Workout();
        if (date != null) {
            try {
                LocalDate localDate = LocalDate.parse(date);
                workout.setDateTime(localDate.atTime(LocalTime.now()));
            } catch (Exception e) {
                workout.setDateTime(LocalDateTime.now());
            }
        } else {
            workout.setDateTime(LocalDateTime.now());
        }
        model.addAttribute("workout", workout);
        return "workouts/new";
    }

    @PostMapping
    public String createWorkout(
        @Valid @ModelAttribute("workout") Workout workout,
        BindingResult bindingResult,
        Principal principal,
        @RequestParam(defaultValue = "true") boolean autoEstimateCalories,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "workouts/new";
        }

        try{
            UUID userId = getUserId(principal);
            workoutService.addWorkout(userId, workout, autoEstimateCalories);
            redirectAttributes.addFlashAttribute("successMessage", "Workout added successfully");
            return "redirect:/workouts";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error saving workout: " + e.getMessage());
            redirectAttributes.addFlashAttribute("workout", workout);
            return "redirect:/workouts/new";
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Workout> updateWorkout(
        @PathVariable UUID id,
        @Valid @RequestBody Workout workout,
        @RequestParam(defaultValue = "true") boolean autoEstimateCalories
    ) {
        Workout updated = workoutService.updateWorkout(id, workout, autoEstimateCalories);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkout(@PathVariable UUID id) {
        workoutService.deleteWorkout(id);
        return ResponseEntity.ok().build();
    }

    private UUID getUserId(Principal principal) {
        String username = principal.getName();
        return userRepository
            .findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"))
            .getId();
    }
}

