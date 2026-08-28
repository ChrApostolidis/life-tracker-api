package com.lifeTracker.life_tracker_api.journalEntries;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * A long-form journal entry. Unlike {@code DayNote} there is no one-per-day
 * constraint — writing three times on a Sunday is the point of this table.
 */
@Entity
@Table(name = "journal_entries")
public class JournalEntry {

    @Id
    private String id;

    // Optional. The list falls back to the first line of the body when absent.
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String body;

    // Comma-separated tag names, the same snapshot convention as WatchItem.genres.
    private String tags;

    // Local calendar day 'YYYY-MM-DD' — the day this entry is about, which is
    // not necessarily the day it was written.
    @Column(nullable = false)
    private String entryDate;

    private Instant deletedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    // getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public String getEntryDate() { return entryDate; }
    public void setEntryDate(String entryDate) { this.entryDate = entryDate; }

    public Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
