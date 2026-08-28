package com.lifeTracker.life_tracker_api.journalEntries;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:3000", "https://lifetracker.christosapostolidis.com"})
public class JournalEntryController {

    private final JournalEntryService journalEntryService;

    public JournalEntryController(JournalEntryService journalEntryService) {
        this.journalEntryService = journalEntryService;
    }

    // Both params or neither: the timeline loads everything, the month jumper
    // can narrow to a window.
    @GetMapping("/journal-entries")
    public ResponseEntity<List<JournalEntry>> listJournalEntries(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) {
        if (from != null && to != null) {
            return ResponseEntity.ok(journalEntryService.list(from, to));
        }
        return ResponseEntity.ok(journalEntryService.list());
    }

    @PostMapping("/journal-entries")
    public ResponseEntity<JournalEntry> createJournalEntry(@Valid @RequestBody JournalEntryCreateRequest request) {
        return ResponseEntity.ok(journalEntryService.create(request));
    }

    @PatchMapping("/journal-entries/{id}")
    public ResponseEntity<JournalEntry> updateJournalEntry(
            @PathVariable String id,
            @Valid @RequestBody JournalEntryUpdateRequest request
    ) {
        return ResponseEntity.ok(journalEntryService.update(id, request));
    }

    @DeleteMapping("/journal-entries/{id}")
    public ResponseEntity<Void> deleteJournalEntry(@PathVariable String id) {
        journalEntryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/journal-entries/{id}/restore")
    public ResponseEntity<Void> restoreJournalEntry(@PathVariable String id) {
        journalEntryService.restore(id);
        return ResponseEntity.noContent().build();
    }
}
