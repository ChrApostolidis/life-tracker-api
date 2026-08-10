package com.lifeTracker.life_tracker_api.dayNotes;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:3000", "https://lifetracker.christosapostolidis.com"})
public class DayNoteController {

    private final DayNoteService dayNoteService;

    public DayNoteController(DayNoteService dayNoteService) {
        this.dayNoteService = dayNoteService;
    }

    @GetMapping("/day-notes")
    public ResponseEntity<List<DayNote>> listDayNotes(
            @RequestParam String from,
            @RequestParam String to
    ) {
        return ResponseEntity.ok(dayNoteService.list(from, to));
    }

    @GetMapping("/day-notes/{date}")
    public ResponseEntity<DayNote> getDayNote(@PathVariable String date) {
        return ResponseEntity.ok(dayNoteService.get(date));
    }

    @PostMapping("/day-notes/{date}")
    public ResponseEntity<DayNote> saveDayNote(@PathVariable String date, @Valid @RequestBody DayNoteSaveRequest request) {
        return ResponseEntity.ok(dayNoteService.save(date, request));
    }

    @DeleteMapping("/day-notes/{date}")
    public ResponseEntity<Void> deleteDayNote(@PathVariable String date) {
        dayNoteService.delete(date);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/day-notes/{date}/restore")
    public ResponseEntity<Void> restoreDayNote(@PathVariable String date) {
        dayNoteService.restore(date);
        return ResponseEntity.noContent().build();
    }
}
