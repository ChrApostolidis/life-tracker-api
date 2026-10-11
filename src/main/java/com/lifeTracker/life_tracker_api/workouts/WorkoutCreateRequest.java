package com.lifeTracker.life_tracker_api.workouts;

import jakarta.validation.constraints.Pattern;

import java.time.Instant;

// Starting a workout. performedOn defaults to today in app.timezone, so the
// server's clock decides; send it only to back-log a past session.
// id and startedAt are for clients that log offline and sync later (the mobile
// app): a client-minted id makes a retried create return the same row instead
// of a duplicate, and startedAt keeps the real start time rather than the sync time.
public record WorkoutCreateRequest(
        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}") String performedOn,
        String notes,
        @Pattern(regexp = ClientIds.PATTERN) String id,
        Instant startedAt
) {}
