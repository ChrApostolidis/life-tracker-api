package com.lifeTracker.life_tracker_api.projects;

import jakarta.persistence.*;
import java.time.Instant;

/** A dated progress note on a project. Many per day, like JournalEntry. */
@Entity
@Table(name = "project_updates")
public class ProjectUpdate {

    @Id
    private String id;

    @Column(nullable = false)
    private String projectId;

    @Column(nullable = false, columnDefinition = "text")
    private String body;

    // Local calendar day 'YYYY-MM-DD' — the day the update is about.
    @Column(nullable = false)
    private String entryDate;

    // 'text' | 'voice' — how this update was captured, same as JournalEntry. Create-only.
    @Column(nullable = false)
    private String source = "text";

    // The original speech-to-text output, kept after edits. Transcript only, no audio.
    @Column(columnDefinition = "text")
    private String rawTranscript;

    private Instant deletedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    // getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getEntryDate() { return entryDate; }
    public void setEntryDate(String entryDate) { this.entryDate = entryDate; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getRawTranscript() { return rawTranscript; }
    public void setRawTranscript(String rawTranscript) { this.rawTranscript = rawTranscript; }

    public Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
