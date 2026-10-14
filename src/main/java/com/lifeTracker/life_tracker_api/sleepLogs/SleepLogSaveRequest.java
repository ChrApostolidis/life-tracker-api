package com.lifeTracker.life_tracker_api.sleepLogs;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

// Request DTO. The ordering and length rules live in SleepLogService.save.
public record SleepLogSaveRequest(
        @NotNull Instant bedAt,
        @NotNull Instant wokeAt
) {}
