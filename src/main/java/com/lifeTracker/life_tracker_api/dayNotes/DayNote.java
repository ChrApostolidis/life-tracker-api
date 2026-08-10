package com.lifeTracker.life_tracker_api.dayNotes;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "day_notes")
public class DayNote {

    @Id
    private String id;

    // Local calendar day 'YYYY-MM-DD'. Unique — one entry per day.
    @Column(nullable = false)
    private String entryDate;

    @Column(nullable = false, columnDefinition = "text")
    private String body;

    // 1–5, purely descriptive — deliberately not fed into XP or attributes.
    private Integer rating;

    private Instant deletedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    // getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEntryDate() { return entryDate; }
    public void setEntryDate(String entryDate) { this.entryDate = entryDate; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
