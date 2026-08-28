package com.lifeTracker.life_tracker_api.journalEntries;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

// Request DTO so clients can't set id/timestamps/deletedAt on create.
// entryDate is optional: absent means "today" in app.timezone, resolved in the
// service so the server's clock decides rather than the caller's.
public record JournalEntryCreateRequest(
        String title,
        @NotBlank String body,
        String tags,
        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}") String entryDate
) {}
