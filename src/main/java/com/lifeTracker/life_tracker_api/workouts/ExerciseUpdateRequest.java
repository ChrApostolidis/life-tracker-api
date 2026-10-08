package com.lifeTracker.life_tracker_api.workouts;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

// PATCH payload — null = leave unchanged, same convention as every other package.
// Sending either rep bound pins the range as an override; going back to the
// training style is its own verb (POST /clear-rep-range), since null can't say it.
public record ExerciseUpdateRequest(
        String name,
        @Pattern(regexp = "chest|back|legs|shoulders|arms|core") String muscleGroup,
        @Pattern(regexp = "barbell|dumbbell|machine|cable|bodyweight") String equipment,
        @Pattern(regexp = "compound|isolation") String kind,
        @Min(1) Integer repMin,
        @Min(1) Integer repMax,
        @Min(0) Integer weightStepGrams
) {}
