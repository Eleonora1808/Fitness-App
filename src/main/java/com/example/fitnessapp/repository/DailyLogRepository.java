package com.example.fitnessapp.repository;

import com.example.fitnessapp.entities.DailyLog;
import com.example.fitnessapp.entities.User;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyLogRepository extends JpaRepository<DailyLog, UUID> {

    DailyLog findByUserAndDate(User user, LocalDate date);

    List<DailyLog> findByUserAndDateBetween(User user, LocalDate start, LocalDate end);
}


