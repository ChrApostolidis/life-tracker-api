package com.lifeTracker.life_tracker_api.projects;

import jakarta.persistence.*;
import java.time.Instant;

/** One card on a project's board. Separate from Task: it never shows on Today/Week. */
@Entity
@Table(name = "project_items")
public class ProjectItem {

    @Id
    private String id;

    @Column(nullable = false)
    private String projectId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    // 'todo' | 'doing' | 'done' | 'blocked'. Changes only through the move endpoint.
    @Column(nullable = false)
    private String stage;

    // 'low' | 'normal' | 'urgent'.
    @Column(nullable = false)
    private String priority = "normal";

    // Local calendar day 'YYYY-MM-DD'.
    private String dueOn;

    // Order within the stage, gapped by 1024 and assigned only by the service.
    @Column(nullable = false)
    private Integer position;

    // Stamped on entering 'done', cleared on leaving it.
    private Instant completedAt;

    private Instant deletedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    // getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getDueOn() { return dueOn; }
    public void setDueOn(String dueOn) { this.dueOn = dueOn; }

    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

    public Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
