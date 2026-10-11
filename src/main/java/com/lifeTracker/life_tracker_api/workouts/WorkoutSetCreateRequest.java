package com.lifeTracker.life_tracker_api.workouts;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

// workoutId comes from the path; position is assigned by the service.
// id is optional and client-minted, so an offline client's retry is idempotent.
public record WorkoutSetCreateRequest(
        @NotBlank String exerciseId,
        @NotNull @Min(0) Integer weightGrams,
        @NotNull @Min(1) Integer reps,
        Boolean warmup,
        @Pattern(regexp = ClientIds.PATTERN) String id
) {}
