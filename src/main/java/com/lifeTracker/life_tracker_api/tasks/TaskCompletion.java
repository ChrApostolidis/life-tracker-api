package com.lifeTracker.life_tracker_api.tasks;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

// One row per completed occurrence of a recurring task template. The pair
// (taskId, occurrenceDate) is unique — completing twice is a no-op, not a
// duplicate row.
@Entity
@Table(name = "task_completions", uniqueConstraints = @UniqueConstraint(columnNames = {"task_id", "occurrence_date"}))
public class TaskCompletion {

    @Id
    private String id;

    @Column(name = "task_id", nullable = false)
    private String taskId;

    // Local calendar day 'YYYY-MM-DD' — matches money_entries.occurred_on's pattern.
    @Column(name = "occurrence_date", nullable = false)
    private String occurrenceDate;

    @Column(name = "completed_at", nullable = false)
    private Instant completedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public String getOccurrenceDate() { return occurrenceDate; }
    public void setOccurrenceDate(String occurrenceDate) { this.occurrenceDate = occurrenceDate; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
