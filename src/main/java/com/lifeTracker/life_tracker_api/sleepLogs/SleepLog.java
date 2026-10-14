package com.lifeTracker.life_tracker_api.sleepLogs;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "sleep_logs")
public class SleepLog {

    @Id
    private String id;

    // Local 'YYYY-MM-DD' of the morning you woke. Unique — one night per day.
    @Column(nullable = false)
    private String sleepDate;

    @Column(nullable = false)
    private Instant bedAt;

    @Column(nullable = false)
    private Instant wokeAt;

    private Instant deletedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    // getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSleepDate() { return sleepDate; }
    public void setSleepDate(String sleepDate) { this.sleepDate = sleepDate; }

    public Instant getBedAt() { return bedAt; }
    public void setBedAt(Instant bedAt) { this.bedAt = bedAt; }

    public Instant getWokeAt() { return wokeAt; }
    public void setWokeAt(Instant wokeAt) { this.wokeAt = wokeAt; }

    public Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
