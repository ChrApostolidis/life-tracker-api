package com.lifeTracker.life_tracker_api.notes;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "notes")
public class Note {

    @Id
    private String id;

    // NULL = standalone note (notes library); non-null = attached to a task.
    private String taskId;

    @Column(nullable = false)
    private String body;

    @Column(nullable = false)
    private String source = "text";

    private String rawTranscript;
    private Instant deletedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    // getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

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
