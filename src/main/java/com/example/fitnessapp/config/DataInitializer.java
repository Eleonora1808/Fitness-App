package com.example.fitnessapp.config;

import com.example.fitnessapp.entities.DailyLog;
import com.example.fitnessapp.entities.Gender;
import com.example.fitnessapp.entities.Goal;
import com.example.fitnessapp.entities.Role;
import com.example.fitnessapp.entities.User;
import com.example.fitnessapp.entities.Workout;
import com.example.fitnessapp.entities.WorkoutType;
import com.example.fitnessapp.repository.DailyLogRepository;
import com.example.fitnessapp.repository.UserRepository;
import com.example.fitnessapp.service.DailyLogService;
import com.example.fitnessapp.service.ProgressService;
import com.example.fitnessapp.service.WorkoutService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DailyLogService dailyLogService;
    private final WorkoutService workoutService;
    private final ProgressService progressService;
    private final DailyLogRepository dailyLogRepository;

    @Value("${fitnessapp.data.initialize:true}")
    private boolean initializeData;

    public DataInitializer(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        DailyLogService dailyLogService,
        WorkoutService workoutService,
        ProgressService progressService,
        DailyLogRepository dailyLogRepository
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.dailyLogService = dailyLogService;
        this.workoutService = workoutService;
        this.progressService = progressService;
        this.dailyLogRepository = dailyLogRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (!initializeData) {
            return;
        }

        User admin = createAdminUser();

        User testUser = createTestUser();

        createComprehensiveSampleData(testUser);
        createComprehensiveSampleData(admin);
    }

    private User createAdminUser() {
        return userRepository.findByUsername("admin")
            .orElseGet(() -> {
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@fitnessapp.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setActive(true);
                admin.setAge(30);
                admin.setGender(Gender.M);
                admin.setHeightCm(180);
                admin.setCurrentWeightKg(new BigDecimal("80.0"));
                admin.setGoal(Goal.MAINTAIN);

                Set<Role> roles = new HashSet<>();
                roles.add(Role.ROLE_USER);
                roles.add(Role.ROLE_ADMIN);
                admin.setRoles(roles);

                return userRepository.save(admin);
            });
    }

    private User createTestUser() {
        return userRepository.findByUsername("testuser")
            .orElseGet(() -> {
                User user = new User();
                user.setUsername("testuser");
                user.setEmail("testuser@fitnessapp.com");
                user.setPassword(passwordEncoder.encode("test123"));
                user.setActive(true);
                user.setAge(25);
                user.setGender(Gender.F);
                user.setHeightCm(165);
                user.setCurrentWeightKg(new BigDecimal("65.0"));
                user.setGoal(Goal.LOSE);

                Set<Role> roles = new HashSet<>();
                roles.add(Role.ROLE_USER);
                user.setRoles(roles);

                return userRepository.save(user);
            });
    }

    private void createComprehensiveSampleData(User user) {
        LocalDate today = LocalDate.now();
        
        for (int i = 0; i < 7; i++) {
            LocalDate date = today.minusDays(i);
            createDailyLogWithVariedWorkouts(user, date, i);
        }

        createDetailedProgressEntries(user);
    }

    private void createDailyLogWithVariedWorkouts(User user, LocalDate date, int dayOffset) {
        DailyLog log = dailyLogRepository.findByUserAndDate(user, date);
        if (log == null) {
            String notes = switch (dayOffset % 3) {
                case 0 -> "Active day - multiple workouts";
                case 1 -> "Moderate activity day";
                case 2 -> "Rest day with light activity";
                default -> "Regular training day";
            };
            log = dailyLogService.createDailyLog(user.getId(), date, notes);
        }

        if (!workoutService.findWorkouts(user.getId(), date, date).isEmpty()) {
            return;
        }

        switch (dayOffset % 7) {
            case 0 -> { 
                createWorkout(user, date, LocalTime.of(8, 0), WorkoutType.STRENGTH, 60, 
                    "Full body strength training - Squats, Deadlifts, Bench Press");
            }
            case 1 -> { 
                createWorkout(user, date, LocalTime.of(7, 0), WorkoutType.CARDIO, 45, 
                    "Morning run - 5km at moderate pace");
                createWorkout(user, date, LocalTime.of(18, 30), WorkoutType.CARDIO, 30, 
                    "Evening cycling session");
            }
            case 2 -> { 
                createWorkout(user, date, LocalTime.of(6, 30), WorkoutType.YOGA, 60, 
                    "Vinyasa flow yoga - Full body stretch and flexibility");
            }
            case 3 -> { 
                createWorkout(user, date, LocalTime.of(7, 30), WorkoutType.STRENGTH, 45, 
                    "Upper body strength - Push and pull exercises");
                createWorkout(user, date, LocalTime.of(19, 0), WorkoutType.CARDIO, 20, 
                    "HIIT session - 20 minutes high intensity");
            }
            case 4 -> { 
                createWorkout(user, date, LocalTime.of(8, 0), WorkoutType.CARDIO, 50, 
                    "Long distance run - 8km steady pace");
            }
            case 5 -> {
                createWorkout(user, date, LocalTime.of(9, 0), WorkoutType.STRENGTH, 50, 
                    "Lower body strength - Legs and glutes");
                createWorkout(user, date, LocalTime.of(17, 0), WorkoutType.YOGA, 30, 
                    "Restorative yoga - Recovery session");
            }
            case 6 -> { 
                if (dayOffset % 2 == 0) {
                    createWorkout(user, date, LocalTime.of(10, 0), WorkoutType.OTHER, 40, 
                        "Outdoor hiking - Nature walk and light climbing");
                } else {
                    createWorkout(user, date, LocalTime.of(8, 0), WorkoutType.YOGA, 45, 
                        "Gentle yoga and meditation");
                }
            }
        }
    }

    private void createWorkout(User user, LocalDate date, LocalTime time, WorkoutType type, 
                               int durationMinutes, String notes) {
        Workout workout = new Workout();
        workout.setDateTime(date.atTime(time));
        workout.setType(type);
        workout.setDurationMinutes(durationMinutes);
        workout.setNotes(notes);
        workoutService.addWorkout(user.getId(), workout, true);
    }

    private void createDetailedProgressEntries(User user) {
        LocalDate today = LocalDate.now();
    
        var existingProgress = progressService.getProgressHistory(
            user.getId(),
            today.minusDays(30),
            today
        );

        if (existingProgress.isEmpty()) {
            BigDecimal baseWeight = user.getCurrentWeightKg();
            Goal goal = user.getGoal();
            
            for (int i = 0; i < 30; i++) {
                LocalDate date = today.minusDays(i);
                
                BigDecimal weightChange;
                if (goal == Goal.LOSE) {
                    weightChange = new BigDecimal(i * 0.015); 
                    weightChange = weightChange.negate();
                } else if (goal == Goal.GAIN) {
                    weightChange = new BigDecimal(i * 0.01); 
                } else {
                    weightChange = new BigDecimal((Math.sin(i / 5.0) * 0.5)); 
                }
                
                BigDecimal weight = baseWeight.add(weightChange);
                
                String notes = switch (i % 7) {
                    case 0 -> "Weekly measurement - Progress check";
                    case 3 -> "Mid-week check-in";
                    case 6 -> "End of week measurement";
                    default -> "Daily tracking";
                };
                
                progressService.addProgress(
                    user.getId(),
                    date,
                    weight,
                    notes
                );
            }
        }
    }
}

