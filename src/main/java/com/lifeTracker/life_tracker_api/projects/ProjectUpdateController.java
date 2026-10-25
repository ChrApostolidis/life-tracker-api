package com.lifeTracker.life_tracker_api.projects;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProjectUpdateController {

    private final ProjectUpdateService projectUpdateService;

    public ProjectUpdateController(ProjectUpdateService projectUpdateService) {
        this.projectUpdateService = projectUpdateService;
    }

    @GetMapping("/projects/{id}/updates")
    public ResponseEntity<List<ProjectUpdate>> listUpdates(@PathVariable String id) {
        return ResponseEntity.ok(projectUpdateService.list(id));
    }

    @PostMapping("/projects/{id}/updates")
    public ResponseEntity<ProjectUpdate> createUpdate(@PathVariable String id, @Valid @RequestBody ProjectUpdateCreateRequest request) {
        return ResponseEntity.ok(projectUpdateService.create(id, request));
    }

    @PatchMapping("/project-updates/{id}")
    public ResponseEntity<ProjectUpdate> updateUpdate(@PathVariable String id, @Valid @RequestBody ProjectUpdatePatchRequest request) {
        return ResponseEntity.ok(projectUpdateService.update(id, request));
    }

    @DeleteMapping("/project-updates/{id}")
    public ResponseEntity<Void> deleteUpdate(@PathVariable String id) {
        projectUpdateService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/project-updates/{id}/restore")
    public ResponseEntity<Void> restoreUpdate(@PathVariable String id) {
        projectUpdateService.restore(id);
        return ResponseEntity.noContent().build();
    }
}
