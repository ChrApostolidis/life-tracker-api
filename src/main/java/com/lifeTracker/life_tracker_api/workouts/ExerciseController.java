package com.lifeTracker.life_tracker_api.workouts;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ExerciseController {

    private final ExerciseService exerciseService;

    public ExerciseController(ExerciseService exerciseService) {
        this.exerciseService = exerciseService;
    }

    @GetMapping("/exercises")
    public ResponseEntity<List<Exercise>> listExercises() {
        return ResponseEntity.ok(exerciseService.list());
    }

    @PostMapping("/exercises")
    public ResponseEntity<Exercise> createExercise(@Valid @RequestBody ExerciseCreateRequest request) {
        return ResponseEntity.ok(exerciseService.create(request));
    }

    @PatchMapping("/exercises/{id}")
    public ResponseEntity<Exercise> updateExercise(@PathVariable String id, @Valid @RequestBody ExerciseUpdateRequest request) {
        return ResponseEntity.ok(exerciseService.update(id, request));
    }

    @PostMapping("/exercises/{id}/clear-rep-range")
    public ResponseEntity<Exercise> clearRepRange(@PathVariable String id) {
        return ResponseEntity.ok(exerciseService.clearRepRange(id));
    }

    @GetMapping("/gym-settings")
    public ResponseEntity<GymSettings> getGymSettings() {
        return ResponseEntity.ok(exerciseService.getSettings());
    }

    @PatchMapping("/gym-settings")
    public ResponseEntity<GymSettings> updateGymSettings(@Valid @RequestBody GymSettingsUpdateRequest request) {
        return ResponseEntity.ok(exerciseService.updateSettings(request));
    }

    @PostMapping("/exercises/{id}/archive")
    public ResponseEntity<Void> archiveExercise(@PathVariable String id) {
        exerciseService.archive(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/exercises/{id}/unarchive")
    public ResponseEntity<Void> unarchiveExercise(@PathVariable String id) {
        exerciseService.unarchive(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exercises/{id}/history")
    public ResponseEntity<List<ExerciseSession>> exerciseHistory(
            @PathVariable String id,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String excludeWorkoutId
    ) {
        return ResponseEntity.ok(exerciseService.history(id, limit, excludeWorkoutId));
    }

    @GetMapping("/exercises/{id}/suggestion")
    public ResponseEntity<ProgressionSuggestion> exerciseSuggestion(
            @PathVariable String id,
            @RequestParam(required = false) String excludeWorkoutId
    ) {
        return ResponseEntity.ok(exerciseService.suggestion(id, excludeWorkoutId));
    }
}
