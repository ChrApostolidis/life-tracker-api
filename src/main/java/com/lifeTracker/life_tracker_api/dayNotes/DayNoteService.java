package com.lifeTracker.life_tracker_api.dayNotes;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class DayNoteService {

    private static final Pattern DATE_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");

    private final DayNoteRepository dayNoteRepository;

    public DayNoteService(DayNoteRepository dayNoteRepository) {
        this.dayNoteRepository = dayNoteRepository;
    }

    public DayNote get(String date) {
        validateDate(date);
        return getLiveOrThrow(date);
    }

    public List<DayNote> list(String from, String to) {
        return dayNoteRepository.findEntriesInRange(from, to);
    }

    // Upsert: create, overwrite, or revive a soft-deleted row for this date.
    // A blank body clears the entry instead of persisting an empty one.
    public DayNote save(String date, DayNoteSaveRequest request) {
        validateDate(date);
        DayNote note = dayNoteRepository.findByEntryDate(date).orElseGet(() -> {
            DayNote fresh = new DayNote();
            fresh.setId(UUID.randomUUID().toString());
            fresh.setEntryDate(date);
            fresh.setCreatedAt(Instant.now());
            return fresh;
        });

        note.setBody(request.body());
        note.setRating(request.rating());
        note.setUpdatedAt(Instant.now());

        if (request.body().isBlank()) {
            note.setDeletedAt(Instant.now());
        } else {
            // Revive path: writing real text to a previously-cleared date
            // must not collide with the unique index on entryDate.
            note.setDeletedAt(null);
        }

        return dayNoteRepository.save(note);
    }

    public void delete(String date) {
        validateDate(date);
        DayNote note = getLiveOrThrow(date);
        note.setDeletedAt(Instant.now());
        note.setUpdatedAt(Instant.now());
        dayNoteRepository.save(note);
    }

    public void restore(String date) {
        validateDate(date);
        DayNote note = dayNoteRepository.findByEntryDate(date)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Day note not found"));
        note.setDeletedAt(null);
        note.setUpdatedAt(Instant.now());
        dayNoteRepository.save(note);
    }

    private void validateDate(String date) {
        if (!DATE_PATTERN.matcher(date).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "date must be YYYY-MM-DD");
        }
    }

    private DayNote getLiveOrThrow(String date) {
        DayNote note = dayNoteRepository.findByEntryDate(date)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Day note not found"));
        if (note.getDeletedAt() != null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Day note not found");
        }
        return note;
    }
}
