package com.lifeTracker.life_tracker_api.projects;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProjectItemController {

    private final ProjectItemService projectItemService;

    public ProjectItemController(ProjectItemService projectItemService) {
        this.projectItemService = projectItemService;
    }

    @GetMapping("/projects/{id}/items")
    public ResponseEntity<List<ProjectItem>> listItems(@PathVariable String id) {
        return ResponseEntity.ok(projectItemService.list(id));
    }

    @PostMapping("/projects/{id}/items")
    public ResponseEntity<ProjectItem> createItem(@PathVariable String id, @Valid @RequestBody ProjectItemCreateRequest request) {
        return ResponseEntity.ok(projectItemService.create(id, request));
    }

    @PatchMapping("/project-items/{id}")
    public ResponseEntity<ProjectItem> updateItem(@PathVariable String id, @Valid @RequestBody ProjectItemPatchRequest request) {
        return ResponseEntity.ok(projectItemService.update(id, request));
    }

    @PostMapping("/project-items/{id}/clear-due")
    public ResponseEntity<ProjectItem> clearItemDue(@PathVariable String id) {
        return ResponseEntity.ok(projectItemService.clearDue(id));
    }

    @PostMapping("/project-items/{id}/move")
    public ResponseEntity<List<ProjectItem>> moveItem(@PathVariable String id, @Valid @RequestBody ProjectItemMoveRequest request) {
        return ResponseEntity.ok(projectItemService.move(id, request));
    }

    @DeleteMapping("/project-items/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable String id) {
        projectItemService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/project-items/{id}/restore")
    public ResponseEntity<Void> restoreItem(@PathVariable String id) {
        projectItemService.restore(id);
        return ResponseEntity.noContent().build();
    }
}
