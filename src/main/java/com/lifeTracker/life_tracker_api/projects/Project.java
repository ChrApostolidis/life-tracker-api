package com.lifeTracker.life_tracker_api.projects;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "projects")
public class Project {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    // One-liner shown on pool cards.
    private String pitch;

    @Column(columnDefinition = "text")
    private String description;

    // 'idea' -> 'ongoing' -> 'finished'.
    @Column(nullable = false)
    private String status;

    // http(s) only — checked by the service so a stored javascript: URL can never reach an href.
    private String repoUrl;
    private String liveUrl;

    // Comma-separated names, the same snapshot convention as JournalEntry.tags.
    private String techStack;

    // Local calendar day 'YYYY-MM-DD', auto-stamped by the service when status
    // moves to ongoing/finished and the field is still empty.
    private String startedOn;
    private String finishedOn;

    private Instant deletedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    // getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPitch() { return pitch; }
    public void setPitch(String pitch) { this.pitch = pitch; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRepoUrl() { return repoUrl; }
    public void setRepoUrl(String repoUrl) { this.repoUrl = repoUrl; }

    public String getLiveUrl() { return liveUrl; }
    public void setLiveUrl(String liveUrl) { this.liveUrl = liveUrl; }

    public String getTechStack() { return techStack; }
    public void setTechStack(String techStack) { this.techStack = techStack; }

    public String getStartedOn() { return startedOn; }
    public void setStartedOn(String startedOn) { this.startedOn = startedOn; }

    public String getFinishedOn() { return finishedOn; }
    public void setFinishedOn(String finishedOn) { this.finishedOn = finishedOn; }

    public Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
