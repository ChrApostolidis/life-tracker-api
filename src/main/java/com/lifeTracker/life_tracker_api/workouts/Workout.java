package com.lifeTracker.life_tracker_api.workouts;

import jakarta.persistence.*;
import java.time.Instant;

/** One gym session. Its sets live in {@link WorkoutSet}, linked by workoutId. */
@Entity
@Table(name = "workouts")
public class Workout {

    @Id
    private String id;

    // Local calendar day 'YYYY-MM-DD', so a late-night session can't slide
    // across UTC midnight onto the wrong day.
    @Column(nullable = false)
    private String performedOn;

    @Column(nullable = false)
    private Instant startedAt;

    // Null = still in progress, or Finish was never tapped. Either way the
    // workout still counts for suggestions.
    private Instant finishedAt;

    @Column(columnDefinition = "text")
    private String notes;

    private Instant deletedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    // getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPerformedOn() { return performedOn; }
    public void setPerformedOn(String performedOn) { this.performedOn = performedOn; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getFinishedAt() { return finishedAt; }
    public void setFinishedAt(Instant finishedAt) { this.finishedAt = finishedAt; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
