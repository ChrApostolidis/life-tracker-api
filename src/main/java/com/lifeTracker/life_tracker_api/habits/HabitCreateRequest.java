package com.lifeTracker.life_tracker_api.habits;

import jakarta.validation.constraints.NotBlank;

// Request DTO so clients can't set id/timestamps/archivedAt on create.
public record HabitCreateRequest(
        @NotBlank String name
) {}
