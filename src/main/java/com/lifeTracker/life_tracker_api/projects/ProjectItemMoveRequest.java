package com.lifeTracker.life_tracker_api.projects;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

// index is among the target stage's OTHER live items; out-of-range values are
// clamped by the service rather than rejected, so a stale client still lands.
public record ProjectItemMoveRequest(
        @NotNull @Pattern(regexp = "todo|doing|done|blocked") String stage,
        @NotNull Integer index
) {}
