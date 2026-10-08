package com.lifeTracker.life_tracker_api.workouts;

import jakarta.persistence.*;
import java.time.Instant;

/** The single settings row (id 'default') for the gym tracker. */
@Entity
@Table(name = "gym_settings")
public class GymSettings {

    static final String ROW_ID = "default";

    @Id
    private String id;

    // 'strength' | 'balanced' | 'hypertrophy'. Null = not chosen yet, which the
    // web app takes as its cue to ask; TrainingStyles.DEFAULT applies meanwhile.
    private String trainingStyle;

    private Instant updatedAt;

    // getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTrainingStyle() { return trainingStyle; }
    public void setTrainingStyle(String trainingStyle) { this.trainingStyle = trainingStyle; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
