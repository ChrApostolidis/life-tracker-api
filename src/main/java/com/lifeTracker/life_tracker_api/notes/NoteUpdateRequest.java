package com.lifeTracker.life_tracker_api.notes;

import jakarta.validation.constraints.NotBlank;

// PATCH payload — only the body is editable; source/rawTranscript stay as captured.
public record NoteUpdateRequest(
        @NotBlank String body
) {}
