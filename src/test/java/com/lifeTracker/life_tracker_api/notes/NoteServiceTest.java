package com.lifeTracker.life_tracker_api.notes;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

// Each test rolls back at the end (Spring test transaction), so tests can
// freely create notes against the shared test DB without cleaning up.
@SpringBootTest
@Transactional
class NoteServiceTest {

    @Autowired
    private NoteService noteService;

    @Test
    void bookNotesAreExcludedFromStandaloneListAndIncludedInBookList() {
        Note standalone = noteService.create(new NoteCreateRequest("just a thought", null, null, null));
        Note bookNote = noteService.create(new NoteCreateRequest("loved chapter 3", null, null, "some-book-id"));

        List<Note> standaloneList = noteService.listStandalone();
        assertTrue(standaloneList.stream().anyMatch(n -> n.getId().equals(standalone.getId())));
        assertTrue(standaloneList.stream().noneMatch(n -> n.getId().equals(bookNote.getId())));

        List<Note> bookList = noteService.listByBookId("some-book-id");
        assertTrue(bookList.stream().anyMatch(n -> n.getId().equals(bookNote.getId())));
        assertTrue(bookList.stream().noneMatch(n -> n.getId().equals(standalone.getId())));
    }
}
