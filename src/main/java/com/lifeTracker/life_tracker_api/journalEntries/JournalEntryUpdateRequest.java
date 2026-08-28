package com.lifeTracker.life_tracker_api.journalEntries;

import jakarta.validation.constraints.Pattern;

// PATCH payload — null = leave unchanged, the same convention as every other
// package. Note the deliberate difference from DayNoteSaveRequest: a blank body
// here is a validation error, not a delete. DayNote is addressed by date and
// has no id to DELETE, so clearing its text is the only way to remove it; a
// journal entry has its own id and its own DELETE endpoint.
public record JournalEntryUpdateRequest(
        String title,
        String body,
        String tags,
        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}") String entryDate
) {}
