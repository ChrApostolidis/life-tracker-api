package com.lifeTracker.life_tracker_api.tasks;

import org.springframework.stereotype.Service;
import java.time.Instant;
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
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (updates.getTitle() != null) task.setTitle(updates.getTitle());
        if (updates.getScheduledAt() != null) task.setScheduledAt(updates.getScheduledAt());
        if (updates.getDurationMin() != null) task.setDurationMin(updates.getDurationMin());
        if (updates.getRecurrence() != null) task.setRecurrence(updates.getRecurrence());

        task.setUpdatedAt(Instant.now());
        return taskRepository.save(task);
    }

    public void deleteTask(String id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        task.setDeletedAt(Instant.now());
        task.setUpdatedAt(Instant.now());
        taskRepository.save(task);
    }

    public void restoreTask(String id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        task.setDeletedAt(null);
        task.setUpdatedAt(Instant.now());
        taskRepository.save(task);
    }
}