package com.lifeTracker.life_tracker_api.notes;

import com.lifeTracker.life_tracker_api.tasks.Task;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:3000", "https://lifetracker.christosapostolidis.com"})
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping("/notes")
    public ResponseEntity<List<Note>> listNotes(@RequestParam(required = false) String bookId) {
        if (bookId != null) {
            return ResponseEntity.ok(noteService.listByBookId(bookId));
        }
        return ResponseEntity.ok(noteService.listStandalone());
    }

    @PostMapping("/notes")
    public ResponseEntity<Note> createNote(@Valid @RequestBody NoteCreateRequest request) {
        return ResponseEntity.ok(noteService.create(request));
    }

    @PatchMapping("/notes/{id}")
    public ResponseEntity<Note> updateNote(@PathVariable String id, @Valid @RequestBody NoteUpdateRequest request) {
        return ResponseEntity.ok(noteService.update(id, request));
    }

    @DeleteMapping("/notes/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable String id) {
        noteService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/notes/{id}/restore")
    public ResponseEntity<Void> restoreNote(@PathVariable String id) {
        noteService.restore(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/notes/{id}/promote")
    public ResponseEntity<Task> promoteNote(@PathVariable String id) {
        return ResponseEntity.ok(noteService.promote(id));
    }
}
