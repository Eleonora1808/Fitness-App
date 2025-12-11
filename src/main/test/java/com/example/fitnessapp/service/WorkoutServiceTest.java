package com.example.fitnessapp.service;

import com.example.fitnessapp.entities.User;
import com.example.fitnessapp.entities.Workout;
import com.example.fitnessapp.entities.WorkoutType;
import com.example.fitnessapp.repository.UserRepository;
import com.example.fitnessapp.repository.WorkoutRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkoutServiceTest {

    @Mock
    private WorkoutRepository workoutRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DailyLogService dailyLogService;

    @InjectMocks
    private WorkoutService workoutService;

    private User testUser;
    private Workout testWorkout;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUser = new User();
        testUser.setId(userId);
        testUser.setCurrentWeightKg(BigDecimal.valueOf(70.0));

        testWorkout = new Workout();
        testWorkout.setType(WorkoutType.CARDIO);
        testWorkout.setDateTime(LocalDateTime.now());
        testWorkout.setDurationMinutes(30);
    }

    @Test
    void testAddWorkout() {
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(testUser));
        when(workoutRepository.save(any(Workout.class))).thenAnswer(invocation -> {
            Workout w = invocation.getArgument(0);
            w.setId(UUID.randomUUID());
            return w;
        });

        Workout result = workoutService.addWorkout(userId, testWorkout, true);

        assertNotNull(result);
        assertNotNull(result.getUser());
        assertEquals(testUser, result.getUser());
        assertNotNull(result.getCaloriesBurned());
        verify(workoutRepository, times(1)).save(any(Workout.class));
        verify(dailyLogService, times(1)).computeDailyTotals(any(UUID.class), any());
    }

    @Test
    void testUpdateWorkout() {
        UUID workoutId = UUID.randomUUID();
        testWorkout.setId(workoutId);
        testWorkout.setUser(testUser);

        Workout updates = new Workout();
        updates.setDurationMinutes(45);
        updates.setNotes("Updated notes");

        when(workoutRepository.findById(workoutId)).thenReturn(java.util.Optional.of(testWorkout));
        when(workoutRepository.save(any(Workout.class))).thenReturn(testWorkout);

        Workout result = workoutService.updateWorkout(workoutId, updates, false);

        assertNotNull(result);
        assertEquals(45, result.getDurationMinutes());
        assertEquals("Updated notes", result.getNotes());
        verify(workoutRepository, times(1)).save(any(Workout.class));
    }

    @Test
    void testDeleteWorkout() {
        UUID workoutId = UUID.randomUUID();
        testWorkout.setId(workoutId);
        testWorkout.setUser(testUser);
        testWorkout.setDateTime(LocalDateTime.now());

        when(workoutRepository.findById(workoutId)).thenReturn(java.util.Optional.of(testWorkout));
        doNothing().when(workoutRepository).delete(any(Workout.class));

        workoutService.deleteWorkout(workoutId);

        verify(workoutRepository, times(1)).delete(any(Workout.class));
        verify(dailyLogService, times(1)).computeDailyTotals(any(UUID.class), any());
    }
}

