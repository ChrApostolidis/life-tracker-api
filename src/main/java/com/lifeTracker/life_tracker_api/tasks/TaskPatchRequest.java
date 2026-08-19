package com.lifeTracker.life_tracker_api.tasks;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

import java.time.Instant;

// PATCH payload — null = leave unchanged (same convention as every other
// package). Clearing a field is a separate verb: POST /api/tasks/{id}/unschedule.
//
// completedAt/deletedAt/source are deliberately absent: they have their own
// endpoints (/complete, /uncomplete, DELETE, /restore) and source is a fact
// about how the task was captured, not something to edit after the fact.
public record TaskPatchRequest(
        String title,
        Instant scheduledAt,
        @Min(1) Integer durationMin,
        @Pattern(regexp = "daily|weekly|monthly") String recurrence,
        @Min(0) @Max(6) Integer recurrenceDay,
        Instant recurrenceUntil
) {}
