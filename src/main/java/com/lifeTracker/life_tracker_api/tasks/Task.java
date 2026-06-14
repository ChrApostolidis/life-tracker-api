package com.lifeTracker.life_tracker_api.tasks;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    private String id;

    @Column(nullable = false)
    private String title;

    private Instant scheduledAt;
    private Integer durationMin;
    private String recurrence;
    private Integer recurrenceDay;
    private Instant recurrenceUntil;
    private Instant completedAt;
    private Instant deletedAt;

    @Column(nullable = false)
    private String source = "text";

    private String rawTranscript;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    // getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Instant getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(Instant scheduledAt) { this.scheduledAt = scheduledAt; }

    public Integer getDurationMin() { return durationMin; }
    public void setDurationMin(Integer durationMin) { this.durationMin = durationMin; }

    public String getRecurrence() { return recurrence; }
    public void setRecurrence(String recurrence) { this.recurrence = recurrence; }

    public Integer getRecurrenceDay() { return recurrenceDay; }
    public void setRecurrenceDay(Integer recurrenceDay) { this.recurrenceDay = recurrenceDay; }

    public Instant getRecurrenceUntil() { return recurrenceUntil; }
    public void setRecurrenceUntil(Instant recurrenceUntil) { this.recurrenceUntil = recurrenceUntil; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

    public Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getRawTranscript() { return rawTranscript; }
    public void setRawTranscript(String rawTranscript) { this.rawTranscript = rawTranscript; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
