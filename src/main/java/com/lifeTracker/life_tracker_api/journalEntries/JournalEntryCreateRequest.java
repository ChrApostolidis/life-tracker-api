package com.lifeTracker.life_tracker_api.journalEntries;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

// Request DTO so clients can't set id/timestamps/deletedAt on create.
// entryDate is optional: absent means "today" in app.timezone, resolved in the
// service so the server's clock decides rather than the caller's.
//
// source/rawTranscript record how the entry was captured. They are create-only,
// like Task's: dictating an entry then editing it is normal, and the original
// transcript is worth keeping — but it is a fact about capture, not something
// to revise afterwards. Only the transcript is stored; no audio, ever.
public record JournalEntryCreateRequest(
        String title,
        @NotBlank String body,
        String tags,
        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}") String entryDate,
        @Pattern(regexp = "text|voice") String source,
        String rawTranscript
) {}
