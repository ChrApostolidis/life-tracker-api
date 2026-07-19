package com.lifeTracker.life_tracker_api.books;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

// PATCH payload — null = leave unchanged (same convention as tasks/money).
// Status transitions to reading/finished auto-stamp startedOn/finishedOn in
// the service when those fields are still empty; sending them explicitly here
// overrides the auto-stamp.
public record BookUpdateRequest(
        String title,
        String author,
        @Pattern(regexp = "wishlist|owned|reading|finished") String status,
        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}") String startedOn,
        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}") String finishedOn,
        @Min(1) @Max(5) Integer rating,
        String notes,
        String coverUrl
) {}
