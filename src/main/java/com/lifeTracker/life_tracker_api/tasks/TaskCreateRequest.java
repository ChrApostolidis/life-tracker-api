package com.lifeTracker.life_tracker_api.tasks;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.Instant;

// Request DTO so clients can't set id/timestamps/completedAt/deletedAt on create,
// and so new entity fields don't silently widen the API. Mirrors the pattern the
// notes/books/watchItems packages already use.
//
// scheduledAt null = goes to the inbox. source defaults to 'text' in the service
// when absent, matching the entity default.
public record TaskCreateRequest(
        @NotBlank String title,
        Instant scheduledAt,
        @Min(1) Integer durationMin,
        @Pattern(regexp = "daily|weekly|monthly") String recurrence,
        @Min(0) @Max(6) Integer recurrenceDay,
        Instant recurrenceUntil,
        @Pattern(regexp = "text|voice") String source,
        String rawTranscript
) {}
