package com.example.fitnessapp.controller;

import com.example.fitnessapp.entities.User;
import com.example.fitnessapp.entities.Workout;
import com.example.fitnessapp.entities.WorkoutType;
import com.example.fitnessapp.repository.UserRepository;
import com.example.fitnessapp.service.WorkoutService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WorkoutController.class)
class WorkoutControllerTest {

    @TestConfiguration
    static class TestWebConfig implements WebMvcConfigurer {
        @Override
        public void addFormatters(FormatterRegistry registry) {
            DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
            registrar.setDateTimeFormatter(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            registrar.setDateFormatter(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
            registrar.setTimeFormatter(java.time.format.DateTimeFormatter.ISO_LOCAL_TIME);
            registrar.registerFormatters(registry);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WorkoutService workoutService;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUser = new User();
        testUser.setId(userId);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setCurrentWeightKg(BigDecimal.valueOf(70.0));
        testUser.setActive(true);
        
        objectMapper.registerModule(new JavaTimeModule());
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testCreateWorkout() throws Exception {
        Workout saved = new Workout();
        saved.setId(UUID.randomUUID());
        saved.setType(WorkoutType.CARDIO);
        saved.setDateTime(LocalDateTime.now());

        when(workoutService.addWorkout(any(UUID.class), any(Workout.class), anyBoolean())).thenReturn(saved);

        LocalDateTime now = LocalDateTime.now();
        String dateTimeStr = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));

        mockMvc.perform(post("/workouts")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("type", "CARDIO")
                .param("dateTime", dateTimeStr)
                .param("durationMinutes", "30")
                .param("autoEstimateCalories", "true"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/workouts"));

        verify(workoutService, times(1)).addWorkout(eq(userId), any(Workout.class), eq(true));
    }

    @Test
    @WithMockUser
    void testUpdateWorkout() throws Exception {
        UUID workoutId = UUID.randomUUID();
        LocalDateTime dateTime = LocalDateTime.now();
        
        Workout workout = new Workout();
        workout.setType(WorkoutType.STRENGTH);
        workout.setDurationMinutes(45);
        workout.setDateTime(dateTime);

        Workout updated = new Workout();
        updated.setId(workoutId);
        updated.setType(WorkoutType.STRENGTH);
        updated.setDurationMinutes(45);
        updated.setDateTime(dateTime);

        when(workoutService.updateWorkout(eq(workoutId), any(Workout.class), anyBoolean())).thenReturn(updated);

        String jsonContent = "{\"type\":\"STRENGTH\",\"durationMinutes\":45,\"dateTime\":\"" + 
            dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "\"}";

        mockMvc.perform(put("/workouts/" + workoutId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent)
                .param("autoEstimateCalories", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(workoutId.toString()))
            .andExpect(jsonPath("$.type").value("STRENGTH"));

        verify(workoutService, times(1)).updateWorkout(eq(workoutId), any(Workout.class), eq(true));
    }

    @Test
    @WithMockUser
    void testDeleteWorkout() throws Exception {
        UUID workoutId = UUID.randomUUID();
        doNothing().when(workoutService).deleteWorkout(workoutId);

        mockMvc.perform(delete("/workouts/" + workoutId)
                .with(csrf()))
            .andExpect(status().isOk());

        verify(workoutService, times(1)).deleteWorkout(workoutId);
    }
}

