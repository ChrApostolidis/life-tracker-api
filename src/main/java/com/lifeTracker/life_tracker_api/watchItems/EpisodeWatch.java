package com.lifeTracker.life_tracker_api.watchItems;

import jakarta.persistence.*;
import java.time.Instant;

// One row = an episode was watched. Un-watching hard-deletes the row (see
// WatchItemService) — same reasoning as HabitCheck: a toggle, not a document,
// and the unique index on (watchItemId, seasonNumber, episodeNumber) requires
// the row to actually be gone to re-watch.
@Entity
@Table(name = "episode_watches")
public class EpisodeWatch {

    @Id
    private String id;

    @Column(nullable = false)
    private String watchItemId;

    @Column(nullable = false)
    private Integer seasonNumber;

    @Column(nullable = false)
    private Integer episodeNumber;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    // getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getWatchItemId() { return watchItemId; }
    public void setWatchItemId(String watchItemId) { this.watchItemId = watchItemId; }

    public Integer getSeasonNumber() { return seasonNumber; }
    public void setSeasonNumber(Integer seasonNumber) { this.seasonNumber = seasonNumber; }

    public Integer getEpisodeNumber() { return episodeNumber; }
    public void setEpisodeNumber(Integer episodeNumber) { this.episodeNumber = episodeNumber; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
