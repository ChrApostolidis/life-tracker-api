package com.lifeTracker.life_tracker_api.tasks;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Task createTask(Task task) {
        task.setId(UUID.randomUUID().toString());
        task.setCreatedAt(Instant.now());
        task.setUpdatedAt(Instant.now());
        return taskRepository.save(task);
    }

    public Optional<Task> getTaskById(String id) {
        return taskRepository.findById(id);
    }

    public List<Task> getInbox() {
        return taskRepository.findByScheduledAtIsNullAndDeletedAtIsNull();
    }

    public Task updateTask(String id, Task updates) {
        Task task = getTaskOrThrow(id);

        if (updates.getTitle() != null) task.setTitle(updates.getTitle());
        if (updates.getScheduledAt() != null) task.setScheduledAt(updates.getScheduledAt());
        if (updates.getDurationMin() != null) task.setDurationMin(updates.getDurationMin());
        if (updates.getRecurrence() != null) task.setRecurrence(updates.getRecurrence());

        task.setUpdatedAt(Instant.now());
        return taskRepository.save(task);
    }

    public void deleteTask(String id) {
        Task task = getTaskOrThrow(id);
        task.setDeletedAt(Instant.now());
        task.setUpdatedAt(Instant.now());
        taskRepository.save(task);
    }

    public void restoreTask(String id) {
        Task task = getTaskOrThrow(id);
        task.setDeletedAt(null);
        task.setUpdatedAt(Instant.now());
        taskRepository.save(task);
    }

    public Task completeTask(String id) {
        Task task = getTaskOrThrow(id);
        task.setCompletedAt(Instant.now());
        task.setUpdatedAt(Instant.now());
        return taskRepository.save(task);
    }

    public Task uncompleteTask(String id) {
        Task task = getTaskOrThrow(id);
        task.setCompletedAt(null);
        task.setUpdatedAt(Instant.now());
        return taskRepository.save(task);
    }

    public List<Task> getTasksInRange(String from, String to) {
        Instant fromInstant = parseInstant(from, "from");
        Instant toInstant = parseInstant(to, "to");
        // Half-open range [from, to): scheduledAt >= from AND scheduledAt < to.
        return taskRepository.findTasksInRange(fromInstant, toInstant);
    }

    // Find a task or fail with 404 (Spring turns ResponseStatusException into the right HTTP status).
    private Task getTaskOrThrow(String id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
    }

    // Parse an ISO-8601 timestamp (e.g. 2026-06-21T00:00:00Z) into an Instant,
    // returning 400 Bad Request instead of 500 on malformed input.
    private Instant parseInstant(String value, String paramName) {
        try {
            return OffsetDateTime.parse(value).toInstant();
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "'" + paramName + "' must be an ISO-8601 timestamp, e.g. 2026-06-21T00:00:00Z");
        }
    }
}
