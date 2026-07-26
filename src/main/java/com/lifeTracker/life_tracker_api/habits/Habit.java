package com.lifeTracker.life_tracker_api.habits;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "habits")
public class Habit {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    // Non-null = hidden from the checklists. Check history is untouched.
    private Instant archivedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    // getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Instant getArchivedAt() { return archivedAt; }
    public void setArchivedAt(Instant archivedAt) { this.archivedAt = archivedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
