package com.lifeTracker.life_tracker_api.habits;

import jakarta.persistence.*;
import java.time.Instant;

// One row = a habit was done on a given local calendar day. Unchecking hard-
// deletes the row (see HabitService) — every other entity in this codebase
// soft-deletes, but a check is a toggle, not a document, and the unique index
// on (habit_id, checked_on) requires the row to actually be gone to re-check.
@Entity
@Table(name = "habit_checks")
public class HabitCheck {

    @Id
    private String id;

    @Column(nullable = false)
    private String habitId;

    // Local calendar day 'YYYY-MM-DD'.
    @Column(nullable = false)
    private String checkedOn;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    // getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getHabitId() { return habitId; }
    public void setHabitId(String habitId) { this.habitId = habitId; }

    public String getCheckedOn() { return checkedOn; }
    public void setCheckedOn(String checkedOn) { this.checkedOn = checkedOn; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
