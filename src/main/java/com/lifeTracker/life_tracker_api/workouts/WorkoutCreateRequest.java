package com.lifeTracker.life_tracker_api.workouts;

import jakarta.validation.constraints.Pattern;

// Starting a workout. performedOn defaults to today in app.timezone, so the
// server's clock decides; send it only to back-log a past session.
public record WorkoutCreateRequest(
        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}") String performedOn,
        String notes
) {}
