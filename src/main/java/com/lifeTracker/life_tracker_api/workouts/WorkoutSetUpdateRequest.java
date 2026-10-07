package com.lifeTracker.life_tracker_api.workouts;

import jakarta.validation.constraints.Min;

// PATCH payload — null = leave unchanged. Moving a set to another exercise or
// workout isn't supported; delete it and log it again.
public record WorkoutSetUpdateRequest(
        @Min(0) Integer weightGrams,
        @Min(1) Integer reps,
        Boolean warmup
) {}
