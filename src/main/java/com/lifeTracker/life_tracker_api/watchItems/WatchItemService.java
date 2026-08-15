package com.lifeTracker.life_tracker_api.watchItems;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
public class WatchItemService {

    private final WatchItemRepository watchItemRepository;
    private final EpisodeWatchRepository episodeWatchRepository;
    private final ZoneId zone;

    public WatchItemService(
            WatchItemRepository watchItemRepository,
            EpisodeWatchRepository episodeWatchRepository,
            @Value("${app.timezone:Europe/Athens}") String timezoneId
    ) {
        this.watchItemRepository = watchItemRepository;
        this.episodeWatchRepository = episodeWatchRepository;
        this.zone = ZoneId.of(timezoneId);
    }

    public List<WatchItem> list() {
        return watchItemRepository.findByDeletedAtIsNullOrderByCreatedAtDesc();
    }

    // Creates a new item, or revives a soft-deleted one for the same
    // (tmdbId, mediaType) pair instead of colliding with the unique index.
    public WatchItem create(WatchItemCreateRequest request) {
        validateStatusForMedia(request.mediaType(), request.status());

        WatchItem existing = watchItemRepository
                .findByTmdbIdAndMediaType(request.tmdbId(), request.mediaType())
                .orElse(null);

        WatchItem item = existing != null ? existing : new WatchItem();
        if (existing == null) {
            item.setId(UUID.randomUUID().toString());
            item.setTmdbId(request.tmdbId());
            item.setMediaType(request.mediaType());
            item.setCreatedAt(Instant.now());
        }

        item.setTitle(request.title());
        item.setYear(request.year());
        item.setPosterUrl(request.posterUrl());
        item.setGenres(request.genres());
        item.setStatus(request.status());
        item.setTotalSeasons(request.totalSeasons());
        item.setTotalEpisodes(request.totalEpisodes());
        item.setDeletedAt(null);
        item.setUpdatedAt(Instant.now());
        stampStatusDates(item, request.status());

        return watchItemRepository.save(item);
    }

    public WatchItem update(String id, WatchItemUpdateRequest request) {
        WatchItem item = getItemOrThrow(id);
        if (item.getDeletedAt() != null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Watch item not found");
        }

        if (request.title() != null) {
            if (request.title().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title must not be blank");
            }
            item.setTitle(request.title());
        }
        if (request.status() != null) {
            validateStatusForMedia(item.getMediaType(), request.status());
            item.setStatus(request.status());
            stampStatusDates(item, request.status());
        }
        if (request.startedOn() != null) item.setStartedOn(request.startedOn());
        if (request.finishedOn() != null) item.setFinishedOn(request.finishedOn());
        if (request.rating() != null) item.setRating(request.rating());
        if (request.notes() != null) item.setNotes(request.notes());
        if (request.posterUrl() != null) item.setPosterUrl(request.posterUrl());
        if (request.genres() != null) item.setGenres(request.genres());

        item.setUpdatedAt(Instant.now());
        return watchItemRepository.save(item);
    }

    public void delete(String id) {
        WatchItem item = getItemOrThrow(id);
        item.setDeletedAt(Instant.now());
        item.setUpdatedAt(Instant.now());
        watchItemRepository.save(item);
    }

    public void restore(String id) {
        WatchItem item = getItemOrThrow(id);
        item.setDeletedAt(null);
        item.setUpdatedAt(Instant.now());
        watchItemRepository.save(item);
    }

    public List<EpisodeWatch> listAllEpisodeWatches() {
        return episodeWatchRepository.findAll();
    }

    // Idempotent: watching an already-watched episode just returns the
    // existing row. May advance the item's status to 'watched' — the response
    // carries the item's current state so the frontend never has to guess.
    public EpisodeWatchResponse watchEpisode(String watchItemId, Integer season, Integer episode) {
        WatchItem item = getItemOrThrow(watchItemId);

        EpisodeWatch watch = episodeWatchRepository
                .findByWatchItemIdAndSeasonNumberAndEpisodeNumber(watchItemId, season, episode)
                .orElseGet(() -> {
                    EpisodeWatch fresh = new EpisodeWatch();
                    fresh.setId(UUID.randomUUID().toString());
                    fresh.setWatchItemId(watchItemId);
                    fresh.setSeasonNumber(season);
                    fresh.setEpisodeNumber(episode);
                    fresh.setCreatedAt(Instant.now());
                    return episodeWatchRepository.save(fresh);
                });

        maybeAdvanceToWatched(item);
        return new EpisodeWatchResponse(watch, item);
    }

    // Hard delete, not a soft delete — see the note on EpisodeWatch. No-op
    // (still succeeds) if the episode was never watched. Does not revert the
    // item's status — silently demoting a finished show because one episode
    // got unticked would be more surprising than helpful.
    public void unwatchEpisode(String watchItemId, Integer season, Integer episode) {
        episodeWatchRepository
                .findByWatchItemIdAndSeasonNumberAndEpisodeNumber(watchItemId, season, episode)
                .ifPresent(episodeWatchRepository::delete);
    }

    // The one business rule beyond CRUD: finishing the last episode of a
    // series that's currently 'watching' auto-advances it to 'watched' and
    // stamps finishedOn, same as manually moving the status pill would.
    private void maybeAdvanceToWatched(WatchItem item) {
        if (item.getTotalEpisodes() == null || !"watching".equals(item.getStatus())) {
            return;
        }
        long watchedCount = episodeWatchRepository.countByWatchItemId(item.getId());
        if (watchedCount >= item.getTotalEpisodes()) {
            item.setStatus("watched");
            if (item.getFinishedOn() == null) {
                item.setFinishedOn(today());
            }
            item.setUpdatedAt(Instant.now());
            watchItemRepository.save(item);
        }
    }

    // A movie has no "part-way through" worth recording, so 'watching' is
    // series-only. Rejected rather than silently coerced — a client sending it
    // has the wrong model of the pipeline and should hear about it.
    private void validateStatusForMedia(String mediaType, String status) {
        if ("watching".equals(status) && !"series".equals(mediaType)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "status 'watching' is only valid for a series");
        }
    }

    // Auto-stamp only when the field is still empty, so an explicit date in the
    // same request wins and a re-entered status doesn't rewrite history.
    private void stampStatusDates(WatchItem item, String status) {
        if ("watching".equals(status) && item.getStartedOn() == null) {
            item.setStartedOn(today());
        }
        if ("watched".equals(status) && item.getFinishedOn() == null) {
            item.setFinishedOn(today());
        }
    }

    private String today() {
        return LocalDate.now(zone).toString();
    }

    private WatchItem getItemOrThrow(String id) {
        return watchItemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Watch item not found"));
    }
}
