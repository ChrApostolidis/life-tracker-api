package com.lifeTracker.life_tracker_api.workouts;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class WorkoutController {

    private final WorkoutService workoutService;

    public WorkoutController(WorkoutService workoutService) {
        this.workoutService = workoutService;
    }

    // Both params or neither, same as journal entries.
    @GetMapping("/workouts")
    public ResponseEntity<List<WorkoutSummaryResponse>> listWorkouts(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) {
        if (from != null && to != null) {
            return ResponseEntity.ok(workoutService.list(from, to));
        }
        return ResponseEntity.ok(workoutService.list());
    }

    @GetMapping("/workouts/{id}")
    public ResponseEntity<WorkoutDetailResponse> getWorkout(@PathVariable String id) {
        return ResponseEntity.ok(workoutService.get(id));
    }

    @PostMapping("/workouts")
    public ResponseEntity<Workout> createWorkout(@Valid @RequestBody WorkoutCreateRequest request) {
        return ResponseEntity.ok(workoutService.create(request));
    }

    @PatchMapping("/workouts/{id}")
    public ResponseEntity<Workout> updateWorkout(@PathVariable String id, @Valid @RequestBody WorkoutUpdateRequest request) {
        return ResponseEntity.ok(workoutService.update(id, request));
    }

    @PostMapping("/workouts/{id}/finish")
    public ResponseEntity<Workout> finishWorkout(@PathVariable String id) {
        return ResponseEntity.ok(workoutService.finish(id));
    }

    @DeleteMapping("/workouts/{id}")
    public ResponseEntity<Void> deleteWorkout(@PathVariable String id) {
        workoutService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/workouts/{id}/restore")
    public ResponseEntity<Void> restoreWorkout(@PathVariable String id) {
        workoutService.restore(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/workouts/{id}/sets")
    public ResponseEntity<WorkoutSet> addSet(@PathVariable String id, @Valid @RequestBody WorkoutSetCreateRequest request) {
        return ResponseEntity.ok(workoutService.addSet(id, request));
    }

    @PatchMapping("/workout-sets/{id}")
    public ResponseEntity<WorkoutSet> updateSet(@PathVariable String id, @Valid @RequestBody WorkoutSetUpdateRequest request) {
        return ResponseEntity.ok(workoutService.updateSet(id, request));
    }

    @DeleteMapping("/workout-sets/{id}")
    public ResponseEntity<Void> deleteSet(@PathVariable String id) {
        workoutService.deleteSet(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/workout-sets/{id}/restore")
    public ResponseEntity<Void> restoreSet(@PathVariable String id) {
        workoutService.restoreSet(id);
        return ResponseEntity.noContent().build();
    }
}
