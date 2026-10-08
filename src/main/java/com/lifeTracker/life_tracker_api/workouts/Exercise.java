package com.lifeTracker.life_tracker_api.workouts;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * A lift you can log sets of. Archived rather than deleted, so a workout's
 * history never points at a missing exercise.
 */
@Entity
@Table(name = "exercises")
public class Exercise {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    // 'chest' | 'back' | 'legs' | 'shoulders' | 'arms' | 'core'
    @Column(nullable = false)
    private String muscleGroup;

    // 'barbell' | 'dumbbell' | 'machine' | 'cable' | 'bodyweight'
    @Column(nullable = false)
    private String equipment;

    // 'compound' | 'isolation' — picks which half of the training style's ranges applies.
    @Column(nullable = false)
    private String kind;

    // A pinned rep range, both set or both null. Null = follow the training style.
    private Integer repMinOverride;

    private Integer repMaxOverride;

    // The rep range in effect, override or style — never persisted. Filled by
    // ExerciseService on every exercise it hands out; Progression reads these.
    @Transient
    private Integer repMin;

    @Transient
    private Integer repMax;

    // What "add weight" adds. 0 = bodyweight, which progresses by reps instead.
    @Column(nullable = false)
    private Integer weightStepGrams;

    private Instant archivedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    // getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMuscleGroup() { return muscleGroup; }
    public void setMuscleGroup(String muscleGroup) { this.muscleGroup = muscleGroup; }

    public String getEquipment() { return equipment; }
    public void setEquipment(String equipment) { this.equipment = equipment; }

    public String getKind() { return kind; }
    public void setKind(String kind) { this.kind = kind; }

    public Integer getRepMinOverride() { return repMinOverride; }
    public void setRepMinOverride(Integer repMinOverride) { this.repMinOverride = repMinOverride; }

    public Integer getRepMaxOverride() { return repMaxOverride; }
    public void setRepMaxOverride(Integer repMaxOverride) { this.repMaxOverride = repMaxOverride; }

    public Integer getRepMin() { return repMin; }
    public void setRepMin(Integer repMin) { this.repMin = repMin; }

    public Integer getRepMax() { return repMax; }
    public void setRepMax(Integer repMax) { this.repMax = repMax; }

    public Integer getWeightStepGrams() { return weightStepGrams; }
    public void setWeightStepGrams(Integer weightStepGrams) { this.weightStepGrams = weightStepGrams; }

    public Instant getArchivedAt() { return archivedAt; }
    public void setArchivedAt(Instant archivedAt) { this.archivedAt = archivedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
