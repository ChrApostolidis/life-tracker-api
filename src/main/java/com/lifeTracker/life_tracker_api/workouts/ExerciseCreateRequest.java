package com.lifeTracker.life_tracker_api.workouts;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

// repMin/repMax are an optional override, both or neither: absent means the
// exercise follows the training style. weightStepGrams absent means the default
// for the equipment (see ExerciseService.defaultStepGrams).
public record ExerciseCreateRequest(
        @NotBlank String name,
        @NotNull @Pattern(regexp = "chest|back|legs|shoulders|arms|core") String muscleGroup,
        @NotNull @Pattern(regexp = "barbell|dumbbell|machine|cable|bodyweight") String equipment,
        @NotNull @Pattern(regexp = "compound|isolation") String kind,
        @Min(1) Integer repMin,
        @Min(1) Integer repMax,
        @Min(0) Integer weightStepGrams
) {}
