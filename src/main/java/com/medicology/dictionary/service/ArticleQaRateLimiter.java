package com.medicology.dictionary.service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;

@Component
public class ArticleQaRateLimiter {

    private final Map<UUID, DailyCounter> counters = new ConcurrentHashMap<>();

    public boolean tryConsume(UUID userId, int dailyLimit) {
        if (dailyLimit <= 0) {
            return true;
        }
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        DailyCounter counter = counters.compute(userId, (ignored, existing) -> {
            if (existing == null || !existing.day.equals(today)) {
                return new DailyCounter(today, new AtomicInteger(0));
            }
            return existing;
        });
        return counter.count.incrementAndGet() <= dailyLimit;
    }

    private record DailyCounter(LocalDate day, AtomicInteger count) {}
}
