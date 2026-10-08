package com.lifeTracker.life_tracker_api.workouts;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

// The style can be changed but not un-chosen: there is no going back to "ask me".
public record GymSettingsUpdateRequest(
        @NotNull @Pattern(regexp = "strength|balanced|hypertrophy") String trainingStyle
) {}
