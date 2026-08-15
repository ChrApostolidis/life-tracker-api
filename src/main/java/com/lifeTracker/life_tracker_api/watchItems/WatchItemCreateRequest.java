package com.lifeTracker.life_tracker_api.watchItems;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

// Request DTO so clients can't set id/timestamps/rating/dates/notes on create —
// a freshly added item has none of those yet. 'watching' is rejected for movies
// by the service, which is where mediaType and status can be checked together.
public record WatchItemCreateRequest(
        @NotNull Integer tmdbId,
        @NotBlank @Pattern(regexp = "movie|series") String mediaType,
        @NotBlank String title,
        String year,
        String posterUrl,
        String genres,
        @NotBlank @Pattern(regexp = "watchlist|watching|watched") String status,
        Integer totalSeasons,
        Integer totalEpisodes
) {}
