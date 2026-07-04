package com.lifeTracker.life_tracker_api.notes;

import com.lifeTracker.life_tracker_api.tasks.Task;
import com.lifeTracker.life_tracker_api.tasks.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final TaskService taskService;

    public NoteService(NoteRepository noteRepository, TaskService taskService) {
        this.noteRepository = noteRepository;
        this.taskService = taskService;
    }

    // Standalone notes only (task_id IS NULL), newest first.
    public List<Note> listStandalone() {
        return noteRepository.findByTaskIdIsNullAndDeletedAtIsNullOrderByCreatedAtDesc();
    }

    public Note create(NoteCreateRequest request) {
        Note note = new Note();
        note.setId(UUID.randomUUID().toString());
        note.setBody(request.body());
        note.setSource(request.source() != null ? request.source() : "text");
        note.setRawTranscript(request.rawTranscript());
        note.setCreatedAt(Instant.now());
        note.setUpdatedAt(Instant.now());
        return noteRepository.save(note);
    }

    public void delete(String id) {
        Note note = getNoteOrThrow(id);
        note.setDeletedAt(Instant.now());
        note.setUpdatedAt(Instant.now());
        noteRepository.save(note);
    }

    public void restore(String id) {
        Note note = getNoteOrThrow(id);
        note.setDeletedAt(null);
        note.setUpdatedAt(Instant.now());
        noteRepository.save(note);
    }

    // Turns a note into an inbox task (scheduledAt stays null) and soft-deletes
    // the note, atomically.
    @Transactional
    public Task promote(String id) {
        Note note = getNoteOrThrow(id);
        if (note.getDeletedAt() != null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found");
        }

        Task task = new Task();
        task.setTitle(note.getBody());
        task.setSource(note.getSource());
        task.setRawTranscript(note.getRawTranscript());
        Task created = taskService.createTask(task);

        note.setDeletedAt(Instant.now());
        note.setUpdatedAt(Instant.now());
        noteRepository.save(note);
        return created;
    }

    private Note getNoteOrThrow(String id) {
        return noteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found"));
    }
}
