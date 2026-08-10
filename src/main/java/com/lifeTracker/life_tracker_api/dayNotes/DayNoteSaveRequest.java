package com.lifeTracker.life_tracker_api.dayNotes;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// Request DTO. body is @NotNull, not @NotBlank — a blank body is the
// documented way to clear an entry (see DayNoteService.save), so it must be
// accepted rather than rejected.
public record DayNoteSaveRequest(
        @NotNull String body,
        @Min(1) @Max(5) Integer rating
) {}
