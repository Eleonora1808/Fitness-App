package com.example.fitnessapp.service;

import com.example.fitnessapp.entities.Progress;
import com.example.fitnessapp.entities.User;
import com.example.fitnessapp.repository.ProgressRepository;
import com.example.fitnessapp.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProgressService {

    private final ProgressRepository progressRepository;
    private final UserRepository userRepository;

    public ProgressService(ProgressRepository progressRepository, UserRepository userRepository) {
        this.progressRepository = progressRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Progress addProgress(UUID userId, LocalDate date, BigDecimal weightKg, String notes) {
        User user = requireUser(userId);
        Progress progress = new Progress();
        progress.setUser(user);
        progress.setDate(date);
        progress.setWeightKg(weightKg);
        progress.setMeasurementNotes(notes);
        return progressRepository.save(progress);
    }

    @Transactional(readOnly = true)
    public List<Progress> getProgressHistory(UUID userId, LocalDate start, LocalDate end) {
        User user = requireUser(userId);
        List<Progress> entries;
        if (start == null || end == null) {
            return progressRepository.findByUserAndDateBetween(user, LocalDate.MIN, LocalDate.MAX);
        } else {
            entries = progressRepository.findByUserAndDateBetween(user, start, end);
        }
        entries.sort(Comparator.comparing(Progress::getDate));
        return entries;
    }

    @Transactional(readOnly = true)
    public ProgressTrend computeTrend(UUID userId, LocalDate start, LocalDate end) {
        List<Progress> entries = getProgressHistory(userId, start, end);
        if (entries.isEmpty()) {
            return ProgressTrend.empty();
        }
        entries.sort(Comparator.comparing(Progress::getDate));
        Progress first = entries.get(0);
        Progress last = entries.get(entries.size() - 1);
        BigDecimal startWeight = first.getWeightKg() != null ? first.getWeightKg() : BigDecimal.ZERO;
        BigDecimal endWeight = last.getWeightKg() != null ? last.getWeightKg() : BigDecimal.ZERO;
        BigDecimal delta = endWeight.subtract(startWeight);
        return new ProgressTrend(startWeight, endWeight, delta, entries.size());
    }

    private User requireUser(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    public record ProgressTrend(BigDecimal startWeight, BigDecimal endWeight, BigDecimal delta, int points) {
        public static ProgressTrend empty() {
            return new ProgressTrend(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0);
        }
    }
}


