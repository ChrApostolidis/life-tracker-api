package com.lifeTracker.life_tracker_api.workouts;

import jakarta.persistence.*;
import java.time.Instant;

/** One logged set. Saved the moment it's logged, so a dead tab mid-workout loses nothing. */
@Entity
@Table(name = "workout_sets")
public class WorkoutSet {

    @Id
    private String id;

    @Column(nullable = false)
    private String workoutId;

    @Column(nullable = false)
    private String exerciseId;

    // Order within the workout, assigned by the service on create.
    @Column(nullable = false)
    private Integer position;

    // Integer grams (62500 = 62.5 kg), per hand for dumbbells.
    @Column(nullable = false)
    private Integer weightGrams;

    @Column(nullable = false)
    private Integer reps;

    // Warm-ups are logged for the record but never count toward the suggestion.
    @Column(nullable = false)
    private Boolean warmup = false;

    private Instant deletedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    // getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getWorkoutId() { return workoutId; }
    public void setWorkoutId(String workoutId) { this.workoutId = workoutId; }

    public String getExerciseId() { return exerciseId; }
    public void setExerciseId(String exerciseId) { this.exerciseId = exerciseId; }

    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }

    public Integer getWeightGrams() { return weightGrams; }
    public void setWeightGrams(Integer weightGrams) { this.weightGrams = weightGrams; }

    public Integer getReps() { return reps; }
    public void setReps(Integer reps) { this.reps = reps; }

    public Boolean getWarmup() { return warmup; }
    public void setWarmup(Boolean warmup) { this.warmup = warmup; }

    public Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
