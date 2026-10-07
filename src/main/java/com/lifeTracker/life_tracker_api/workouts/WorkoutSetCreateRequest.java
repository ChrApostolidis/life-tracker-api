package com.lifeTracker.life_tracker_api.workouts;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// workoutId comes from the path; position is assigned by the service.
public record WorkoutSetCreateRequest(
        @NotBlank String exerciseId,
        @NotNull @Min(0) Integer weightGrams,
        @NotNull @Min(1) Integer reps,
        Boolean warmup
) {}
