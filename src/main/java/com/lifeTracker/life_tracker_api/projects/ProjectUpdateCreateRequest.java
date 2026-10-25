package com.lifeTracker.life_tracker_api.projects;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

// entryDate defaults to today in app.timezone. source/rawTranscript are
// create-only capture facts, the same rule as JournalEntryCreateRequest.
public record ProjectUpdateCreateRequest(
        @NotBlank String body,
        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}") String entryDate,
        @Pattern(regexp = "text|voice") String source,
        String rawTranscript
) {}
