package com.example.fitnessapp.repository;

import com.example.fitnessapp.entities.Progress;
import com.example.fitnessapp.entities.User;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgressRepository extends JpaRepository<Progress, UUID> {

    Optional<Progress> findTopByUserOrderByDateDesc(User user);

    List<Progress> findByUserAndDateBetween(User user, LocalDate start, LocalDate end);
}


