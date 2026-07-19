package com.lifeTracker.life_tracker_api.books;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

// Request DTO so clients can't set id/timestamps/dates/rating/notes on create —
// a freshly added book has none of those yet.
public record BookCreateRequest(
        @NotBlank String title,
        String author,
        @NotBlank @Pattern(regexp = "wishlist|owned|reading|finished") String status,
        String coverUrl
) {}
