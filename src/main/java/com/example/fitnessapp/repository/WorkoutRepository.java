package com.example.fitnessapp.repository;

import com.example.fitnessapp.entities.User;
import com.example.fitnessapp.entities.Workout;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutRepository extends JpaRepository<Workout, UUID> {

    List<Workout> findByUserAndDateTimeBetween(User user, LocalDateTime start, LocalDateTime end);

    default List<Workout> findByUserAndDateBetween(User user, LocalDate start, LocalDate end) {
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.plusDays(1).atStartOfDay();
        return findByUserAndDateTimeBetween(user, startDateTime, endDateTime);
    }
}


