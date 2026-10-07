package com.lifeTracker.life_tracker_api.workouts;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

// weightStepGrams is optional: absent means the default for the equipment
// (see ExerciseService.defaultStepGrams). repMin <= repMax is checked in the service.
public record ExerciseCreateRequest(
        @NotBlank String name,
        @NotNull @Pattern(regexp = "chest|back|legs|shoulders|arms|core") String muscleGroup,
        @NotNull @Pattern(regexp = "barbell|dumbbell|machine|cable|bodyweight") String equipment,
        @NotNull @Min(1) Integer repMin,
        @NotNull @Min(1) Integer repMax,
        @Min(0) Integer weightStepGrams
) {}
