package com.lifeTracker.life_tracker_api.notes;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

// Request DTO instead of binding the entity directly, so clients can't set
// id/timestamps/deletedAt on create. rawTranscript is the original speech-to-text
// (text only — no audio is ever stored), kept even if the body was edited.
// bookId attaches this note to a book's thought stream instead of the
// standalone notes library.
public record NoteCreateRequest(
        @NotBlank String body,
        @Pattern(regexp = "text|voice") String source,
        String rawTranscript,
        String bookId
) {}
