package com.lifeTracker.life_tracker_api.journalEntries;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

@Service
public class JournalEntryService {

    private final JournalEntryRepository journalEntryRepository;
    private final ZoneId zone;

    public JournalEntryService(
            JournalEntryRepository journalEntryRepository,
            @Value("${app.timezone:Europe/Athens}") String timezoneId
    ) {
        this.journalEntryRepository = journalEntryRepository;
        this.zone = ZoneId.of(timezoneId);
    }

    public List<JournalEntry> list() {
        return journalEntryRepository.findByDeletedAtIsNullOrderByEntryDateDescCreatedAtDesc();
    }

    public List<JournalEntry> list(String from, String to) {
        validateDate(from, "from");
        validateDate(to, "to");
        return journalEntryRepository.findEntriesInRange(from, to);
    }

    public JournalEntry create(JournalEntryCreateRequest request) {
        JournalEntry entry = new JournalEntry();
        entry.setId(UUID.randomUUID().toString());
        entry.setTitle(blankToNull(request.title()));
        entry.setBody(request.body());
        entry.setTags(blankToNull(request.tags()));
        // No entryDate means "today" — resolved here so the server's clock and
        // configured timezone decide, not the caller's.
        entry.setEntryDate(request.entryDate() != null ? request.entryDate() : today());
        // Absent source keeps the entity default ('text') rather than nulling a
        // non-nullable column. source/rawTranscript are capture facts and are
        // deliberately not editable afterwards — same rule as Task.
        if (request.source() != null) entry.setSource(request.source());
        entry.setRawTranscript(blankToNull(request.rawTranscript()));
        entry.setCreatedAt(Instant.now());
        entry.setUpdatedAt(Instant.now());
        return journalEntryRepository.save(entry);
    }

    public JournalEntry update(String id, JournalEntryUpdateRequest request) {
        JournalEntry entry = getLiveEntryOrThrow(id);

        if (request.title() != null) entry.setTitle(blankToNull(request.title()));
        if (request.body() != null) {
            if (request.body().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "body must not be blank");
            }
            entry.setBody(request.body());
        }
        if (request.tags() != null) entry.setTags(blankToNull(request.tags()));
        if (request.entryDate() != null) entry.setEntryDate(request.entryDate());

        entry.setUpdatedAt(Instant.now());
        return journalEntryRepository.save(entry);
    }

    public void delete(String id) {
        JournalEntry entry = getLiveEntryOrThrow(id);
        entry.setDeletedAt(Instant.now());
        entry.setUpdatedAt(Instant.now());
        journalEntryRepository.save(entry);
    }

    public void restore(String id) {
        JournalEntry entry = getEntryOrThrow(id);
        entry.setDeletedAt(null);
        entry.setUpdatedAt(Instant.now());
        journalEntryRepository.save(entry);
    }

    // An empty tag string would otherwise persist as "" and read as a tag.
    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String today() {
        return LocalDate.now(zone).toString();
    }

    private void validateDate(String value, String paramName) {
        try {
            LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'" + paramName + "' must be 'YYYY-MM-DD'");
        }
    }

    // Includes soft-deleted rows — only /restore should use this.
    private JournalEntry getEntryOrThrow(String id) {
        return journalEntryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Journal entry not found"));
    }

    private JournalEntry getLiveEntryOrThrow(String id) {
        JournalEntry entry = getEntryOrThrow(id);
        if (entry.getDeletedAt() != null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Journal entry not found");
        }
        return entry;
    }
}
