package com.lifeTracker.life_tracker_api.habits;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:3000", "https://lifetracker.christosapostolidis.com"})
public class HabitController {

    private final HabitService habitService;

    public HabitController(HabitService habitService) {
        this.habitService = habitService;
    }

    @GetMapping("/habits")
    public ResponseEntity<List<Habit>> listHabits() {
        return ResponseEntity.ok(habitService.list());
    }

    @PostMapping("/habits")
    public ResponseEntity<Habit> createHabit(@Valid @RequestBody HabitCreateRequest request) {
        return ResponseEntity.ok(habitService.create(request));
    }

    @PatchMapping("/habits/{id}")
    public ResponseEntity<Habit> updateHabit(@PathVariable String id, @Valid @RequestBody HabitUpdateRequest request) {
        return ResponseEntity.ok(habitService.update(id, request));
    }

    @PostMapping("/habits/{id}/archive")
    public ResponseEntity<Void> archiveHabit(@PathVariable String id) {
        habitService.archive(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/habits/{id}/unarchive")
    public ResponseEntity<Void> unarchiveHabit(@PathVariable String id) {
        habitService.unarchive(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/habit-checks")
    public ResponseEntity<List<HabitCheck>> listChecks(
            @RequestParam String from,
            @RequestParam String to
    ) {
        return ResponseEntity.ok(habitService.listChecks(from, to));
    }

    @PostMapping("/habits/{id}/checks/{date}")
    public ResponseEntity<HabitCheck> checkHabit(@PathVariable String id, @PathVariable String date) {
        return ResponseEntity.ok(habitService.check(id, date));
    }

    @DeleteMapping("/habits/{id}/checks/{date}")
    public ResponseEntity<Void> uncheckHabit(@PathVariable String id, @PathVariable String date) {
        habitService.uncheck(id, date);
        return ResponseEntity.noContent().build();
    }
}
