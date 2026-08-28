package com.lifeTracker.life_tracker_api.journalEntries;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Each test rolls back at the end (Spring test transaction), so tests can
// freely create entries against the shared test DB without cleaning up.
@SpringBootTest
@Transactional
class JournalEntryServiceTest {

    @Autowired
    private JournalEntryService journalEntryService;

    private JournalEntry create(String title, String body, String tags, String date) {
        return journalEntryService.create(new JournalEntryCreateRequest(title, body, tags, date));
    }

    @Test
    void manyEntriesOnTheSameDateAreAllowed() {
        // The regression this table exists for: day_notes has a UNIQUE on
        // entry_date, and journal_entries deliberately does not.
        create("Morning", "First thing today", null, "2026-08-28");
        create("Evening", "Second thing today", null, "2026-08-28");
        JournalEntry third = create(null, "Third, untitled", null, "2026-08-28");

        assertNotNull(third.getId());
        List<JournalEntry> onThatDay = journalEntryService.list("2026-08-28", "2026-08-29");
        assertEquals(3, onThatDay.size());
    }

    @Test
    void entryDateDefaultsToTodayWhenOmitted() {
        JournalEntry entry = create(null, "No date given", null, null);
        assertNotNull(entry.getEntryDate());
        assertTrue(entry.getEntryDate().matches("\\d{4}-\\d{2}-\\d{2}"));
    }

    @Test
    void blankTitleAndTagsAreStoredAsNull() {
        // An empty string would come back as a tag and render an empty chip.
        JournalEntry entry = create("   ", "Body text", "  ", "2026-08-28");
        assertNull(entry.getTitle());
        assertNull(entry.getTags());
    }

    @Test
    void patchingBlankBodyIsRejected() {
        // Unlike DayNote, where a blank body is the documented way to clear an
        // entry — this one has its own DELETE endpoint.
        JournalEntry entry = create(null, "Real body", null, "2026-08-28");
        assertThrows(ResponseStatusException.class, () -> journalEntryService.update(
                entry.getId(), new JournalEntryUpdateRequest(null, "   ", null, null)));
    }

    @Test
    void softDeletedEntryIsInvisibleAndImmutable() {
        JournalEntry entry = create(null, "Gone", null, "2026-08-28");
        journalEntryService.delete(entry.getId());

        assertTrue(journalEntryService.list().stream().noneMatch(e -> e.getId().equals(entry.getId())));
        assertThrows(ResponseStatusException.class, () -> journalEntryService.update(
                entry.getId(), new JournalEntryUpdateRequest(null, "New body", null, null)));

        // Restore is the one verb that still reaches a deleted row.
        journalEntryService.restore(entry.getId());
        assertTrue(journalEntryService.list().stream().anyMatch(e -> e.getId().equals(entry.getId())));
    }

    @Test
    void rangeIsHalfOpenAndOrderedNewestFirst() {
        create(null, "In range, older", null, "2026-08-01");
        create(null, "In range, newer", null, "2026-08-30");
        create(null, "On the exclusive bound", null, "2026-09-01");

        List<JournalEntry> august = journalEntryService.list("2026-08-01", "2026-09-01");

        assertEquals(2, august.size());
        assertEquals("2026-08-30", august.get(0).getEntryDate());
        assertEquals("2026-08-01", august.get(1).getEntryDate());
    }

    @Test
    void malformedRangeDatesAreRejected() {
        assertThrows(ResponseStatusException.class, () -> journalEntryService.list("not-a-date", "2026-09-01"));
    }
}
