package com.lifeTracker.life_tracker_api.projects;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping("/projects")
    public ResponseEntity<List<ProjectSummaryResponse>> listProjects() {
        return ResponseEntity.ok(projectService.list());
    }

    @GetMapping("/projects/{id}")
    public ResponseEntity<Project> getProject(@PathVariable String id) {
        return ResponseEntity.ok(projectService.get(id));
    }

    @PostMapping("/projects")
    public ResponseEntity<Project> createProject(@Valid @RequestBody ProjectCreateRequest request) {
        return ResponseEntity.ok(projectService.create(request));
    }

    @PatchMapping("/projects/{id}")
    public ResponseEntity<Project> updateProject(@PathVariable String id, @Valid @RequestBody ProjectPatchRequest request) {
        return ResponseEntity.ok(projectService.update(id, request));
    }

    @DeleteMapping("/projects/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable String id) {
        projectService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/projects/{id}/restore")
    public ResponseEntity<Void> restoreProject(@PathVariable String id) {
        projectService.restore(id);
        return ResponseEntity.noContent().build();
    }
}
