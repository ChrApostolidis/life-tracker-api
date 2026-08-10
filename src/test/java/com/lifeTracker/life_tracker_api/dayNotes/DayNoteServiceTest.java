package com.lifeTracker.life_tracker_api.dayNotes;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Each test rolls back at the end (Spring test transaction), so tests can
// freely create day notes against the shared test DB without cleaning up.
@SpringBootTest
@Transactional
class DayNoteServiceTest {

    @Autowired
    private DayNoteService dayNoteService;

    @Test
    void saveOnFreshDateCreatesTheRow() {
        DayNote note = dayNoteService.save("2026-03-01", new DayNoteSaveRequest("Good day", null));
        assertNotNull(note.getId());
        assertNotNull(note.getCreatedAt());
        assertEquals("Good day", note.getBody());
    }

    @Test
    void saveTwiceOnSameDateUpdatesRatherThanDuplicating() {
        DayNote first = dayNoteService.save("2026-03-02", new DayNoteSaveRequest("First draft", null));
        DayNote second = dayNoteService.save("2026-03-02", new DayNoteSaveRequest("Final draft", 4));
        assertEquals(first.getId(), second.getId());
        assertEquals("Final draft", second.getBody());

        List<DayNote> inRange = dayNoteService.list("2026-03-01", "2026-04-01");
        long count = inRange.stream().filter(d -> d.getEntryDate().equals("2026-03-02")).count();
        assertEquals(1, count);
    }

    @Test
    void blankBodySoftDeletesAndDropsFromList() {
        dayNoteService.save("2026-03-03", new DayNoteSaveRequest("Something", null));
        dayNoteService.save("2026-03-03", new DayNoteSaveRequest("", null));

        List<DayNote> inRange = dayNoteService.list("2026-03-01", "2026-04-01");
        assertTrue(inRange.stream().noneMatch(d -> d.getEntryDate().equals("2026-03-03")));
        assertThrows(ResponseStatusException.class, () -> dayNoteService.get("2026-03-03"));
    }

    @Test
    void saveAfterBlankClearRevivesTheSameRow() {
        DayNote original = dayNoteService.save("2026-03-04", new DayNoteSaveRequest("Something", null));
        dayNoteService.save("2026-03-04", new DayNoteSaveRequest("", null));

        // Regression test: this must revive the existing row, not attempt an
        // insert that collides with the unique index on entryDate.
        DayNote revived = dayNoteService.save("2026-03-04", new DayNoteSaveRequest("Back again", null));
        assertEquals(original.getId(), revived.getId());
        assertEquals("Back again", revived.getBody());

        List<DayNote> inRange = dayNoteService.list("2026-03-01", "2026-04-01");
        assertTrue(inRange.stream().anyMatch(d -> d.getEntryDate().equals("2026-03-04")));
    }

    @Test
    void listRespectsHalfOpenRange() {
        dayNoteService.save("2026-04-01", new DayNoteSaveRequest("In range", null));
        dayNoteService.save("2026-05-01", new DayNoteSaveRequest("Out of range", null));

        List<DayNote> inRange = dayNoteService.list("2026-04-01", "2026-05-01");
        List<String> dates = inRange.stream().map(DayNote::getEntryDate).toList();
        assertTrue(dates.contains("2026-04-01"));
        assertTrue(!dates.contains("2026-05-01"));
    }

    @Test
    void ratingIsPersisted() {
        DayNote note = dayNoteService.save("2026-03-05", new DayNoteSaveRequest("Great day", 5));
        assertEquals(5, note.getRating());
    }
}
