package com.lifeTracker.life_tracker_api.tasks;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:3000", "https://lifetracker.christosapostolidis.com"})
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/tasks")
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        return ResponseEntity.ok(taskService.createTask(task));
    }

    @GetMapping("/tasks/{id}")
    public ResponseEntity<Task> getTask(@PathVariable String id) {
        return taskService.getTaskById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/tasks")
    public ResponseEntity<List<Task>> getTasksInRange(
            @RequestParam String from,
            @RequestParam String to) {
        return ResponseEntity.ok(taskService.getTasksInRange(from, to));
    }

    @GetMapping("/inbox")
    public ResponseEntity<List<Task>> getInbox() {
        return ResponseEntity.ok(taskService.getInbox());
    }

    @PatchMapping("/tasks/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable String id, @RequestBody Task updates) {
        return ResponseEntity.ok(taskService.updateTask(id, updates));
    }

    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable String id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/tasks/{id}/restore")
    public ResponseEntity<Void> restoreTask(@PathVariable String id) {
        taskService.restoreTask(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/tasks/{id}/complete")
    public ResponseEntity<Task> completeTask(@PathVariable String id) {
        return ResponseEntity.ok(taskService.completeTask(id));
    }

    @PostMapping("/tasks/{id}/uncomplete")
    public ResponseEntity<Task> uncompleteTask(@PathVariable String id) {
        return ResponseEntity.ok(taskService.uncompleteTask(id));
    }

}