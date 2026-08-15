package com.lifeTracker.life_tracker_api.watchItems;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

// PATCH payload — null = leave unchanged (same convention as tasks/money/books).
// Status transitions to watching/watched auto-stamp startedOn/finishedOn in the
// service when those fields are still empty; sending them explicitly overrides.
// 'watching' is series-only — the service rejects it for a movie.
public record WatchItemUpdateRequest(
        String title,
        @Pattern(regexp = "watchlist|watching|watched") String status,
        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}") String startedOn,
        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}") String finishedOn,
        @Min(1) @Max(5) Integer rating,
        String notes,
        String posterUrl,
        String genres
) {}
