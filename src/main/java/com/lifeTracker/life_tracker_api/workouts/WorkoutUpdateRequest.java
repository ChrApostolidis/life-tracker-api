package com.lifeTracker.life_tracker_api.workouts;

import jakarta.validation.constraints.Pattern;

// PATCH payload — null = leave unchanged. Finishing has its own verb.
public record WorkoutUpdateRequest(
        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}") String performedOn,
        String notes
) {}
