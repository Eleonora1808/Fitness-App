package com.example.fitnessapp.service;

import com.example.fitnessapp.repository.DailyLogRepository;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScheduledTasks {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);

    private final DailyLogRepository dailyLogRepository;
    private final CacheService cacheService;

    public ScheduledTasks(DailyLogRepository dailyLogRepository, CacheService cacheService) {
        this.dailyLogRepository = dailyLogRepository;
        this.cacheService = cacheService;
    }

    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void dailyCleanupTask() {
        logger.info("Starting daily cleanup task at 2 AM");
        try {
            LocalDate cutoffDate = LocalDate.now().minusDays(90);
            var oldLogs = dailyLogRepository.findAll().stream()
                .filter(log -> log.getDate().isBefore(cutoffDate))
                .toList();
            dailyLogRepository.deleteAll(oldLogs);
            logger.info("Daily cleanup completed. Deleted {} old daily logs", oldLogs.size());
        } catch (Exception e) {
            logger.error("Error during daily cleanup task", e);
        }
    }

    @Scheduled(fixedRate = 3600000)
    public void hourlyCacheRefresh() {
        logger.debug("Starting hourly cache refresh task");
        try {
            cacheService.clear("foodSearchCache");
            cacheService.clear("foodCache");
            cacheService.clear("reportCache");
            logger.info("Cache refresh completed successfully");
        } catch (Exception e) {
            logger.error("Error during cache refresh task", e);
        }
    }
}

