package com.lifeTracker.life_tracker_api.books;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "books")
public class Book {

    @Id
    private String id;

    @Column(nullable = false)
    private String title;

    private String author;

    // 'wishlist' (want to buy) -> 'owned' (bought) -> 'reading' -> 'finished'.
    @Column(nullable = false)
    private String status;

    // Local calendar day 'YYYY-MM-DD', auto-stamped by the service when status
    // moves to reading/finished and the field is still empty.
    private String startedOn;
    private String finishedOn;

    private Integer rating;
    private String coverUrl;

    @Column(columnDefinition = "text")
    private String notes;

    private Instant deletedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    // getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStartedOn() { return startedOn; }
    public void setStartedOn(String startedOn) { this.startedOn = startedOn; }

    public String getFinishedOn() { return finishedOn; }
    public void setFinishedOn(String finishedOn) { this.finishedOn = finishedOn; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
