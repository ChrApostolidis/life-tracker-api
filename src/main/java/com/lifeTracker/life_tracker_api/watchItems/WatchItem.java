package com.lifeTracker.life_tracker_api.watchItems;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "watch_items")
public class WatchItem {

    @Id
    private String id;

    @Column(nullable = false)
    private Integer tmdbId;

    // 'movie' | 'series'
    @Column(nullable = false)
    private String mediaType;

    @Column(nullable = false)
    private String title;

    private String year;
    private String posterUrl;

    // 'watchlist' -> 'watched'. Series may also sit at 'watching' in between;
    // movies may not — see WatchItemService.validateStatusForMedia.
    @Column(nullable = false)
    private String status;

    // Comma-separated TMDB genre names, snapshotted at add time.
    private String genres;

    private Integer rating;

    // Local calendar day 'YYYY-MM-DD', auto-stamped by the service when status
    // moves to watching/watched and the field is still empty.
    private String startedOn;
    private String finishedOn;

    // Series only; snapshotted from TMDB at add time.
    private Integer totalSeasons;
    private Integer totalEpisodes;

    @Column(columnDefinition = "text")
    private String notes;

    private Instant deletedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    // getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Integer getTmdbId() { return tmdbId; }
    public void setTmdbId(Integer tmdbId) { this.tmdbId = tmdbId; }

    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }

    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getGenres() { return genres; }
    public void setGenres(String genres) { this.genres = genres; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getStartedOn() { return startedOn; }
    public void setStartedOn(String startedOn) { this.startedOn = startedOn; }

    public String getFinishedOn() { return finishedOn; }
    public void setFinishedOn(String finishedOn) { this.finishedOn = finishedOn; }

    public Integer getTotalSeasons() { return totalSeasons; }
    public void setTotalSeasons(Integer totalSeasons) { this.totalSeasons = totalSeasons; }

    public Integer getTotalEpisodes() { return totalEpisodes; }
    public void setTotalEpisodes(Integer totalEpisodes) { this.totalEpisodes = totalEpisodes; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
