package com.lifeTracker.life_tracker_api.sleepLogs;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class SleepLogService {

    private static final Pattern DATE_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
    // Longer than any real night; anything past it is a mistyped date, not sleep.
    private static final Duration MAX_SLEEP = Duration.ofHours(24);

    private final SleepLogRepository sleepLogRepository;

    public SleepLogService(SleepLogRepository sleepLogRepository) {
        this.sleepLogRepository = sleepLogRepository;
    }

    public List<SleepLog> list(String from, String to) {
        validateDate(from);
        validateDate(to);
        return sleepLogRepository.findLogsInRange(from, to);
    }

    // Upsert: create, overwrite, or revive a soft-deleted night for this date.
    public SleepLog save(String date, SleepLogSaveRequest request) {
        validateDate(date);
        validateTimes(request.bedAt(), request.wokeAt());

        SleepLog log = sleepLogRepository.findBySleepDate(date).orElseGet(() -> {
            SleepLog fresh = new SleepLog();
            fresh.setId(UUID.randomUUID().toString());
            fresh.setSleepDate(date);
            fresh.setCreatedAt(Instant.now());
            return fresh;
        });

        log.setBedAt(request.bedAt());
        log.setWokeAt(request.wokeAt());
        log.setDeletedAt(null);
        log.setUpdatedAt(Instant.now());
        return sleepLogRepository.save(log);
    }

    public void delete(String date) {
        validateDate(date);
        SleepLog log = sleepLogRepository.findBySleepDate(date)
                .filter(s -> s.getDeletedAt() == null)
                .orElseThrow(SleepLogService::notFound);
        log.setDeletedAt(Instant.now());
        log.setUpdatedAt(Instant.now());
        sleepLogRepository.save(log);
    }

    public void restore(String date) {
        validateDate(date);
        SleepLog log = sleepLogRepository.findBySleepDate(date).orElseThrow(SleepLogService::notFound);
        log.setDeletedAt(null);
        log.setUpdatedAt(Instant.now());
        sleepLogRepository.save(log);
    }

    private void validateDate(String date) {
        if (!DATE_PATTERN.matcher(date).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "date must be YYYY-MM-DD");
        }
    }

    private void validateTimes(Instant bedAt, Instant wokeAt) {
        if (!wokeAt.isAfter(bedAt)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "wokeAt must be after bedAt");
        }
        if (Duration.between(bedAt, wokeAt).compareTo(MAX_SLEEP) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "a night cannot be longer than 24 hours");
        }
    }

    private static ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Sleep log not found");
    }
}
