package com.lifeTracker.life_tracker_api.books;

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
// freely create books against the shared test DB without cleaning up.
@SpringBootTest
@Transactional
class BookServiceTest {

    @Autowired
    private BookService bookService;

    private Book create(String title, String status) {
        return bookService.create(new BookCreateRequest(title, "Some Author", status, null));
    }

    @Test
    void createSetsDefaults() {
        Book book = create("Dune", "wishlist");
        assertNotNull(book.getId());
        assertNotNull(book.getCreatedAt());
        assertNull(book.getDeletedAt());
        assertNull(book.getStartedOn());
        assertNull(book.getFinishedOn());
        assertEquals("wishlist", book.getStatus());
    }

    @Test
    void movingToReadingStampsStartedOnOnlyWhenEmpty() {
        Book book = create("Dune", "owned");
        Book updated = bookService.update(book.getId(), new BookUpdateRequest(
                null, null, "reading", null, null, null, null, null));
        assertNotNull(updated.getStartedOn());

        // Moving to reading again (e.g. re-selecting the same status) must not
        // overwrite an already-set startedOn.
        String firstStartedOn = updated.getStartedOn();
        Book updatedAgain = bookService.update(book.getId(), new BookUpdateRequest(
                null, null, "reading", null, null, null, null, null));
        assertEquals(firstStartedOn, updatedAgain.getStartedOn());
    }

    @Test
    void movingToFinishedStampsFinishedOnOnlyWhenEmpty() {
        Book book = create("Dune", "reading");
        Book updated = bookService.update(book.getId(), new BookUpdateRequest(
                null, null, "finished", null, null, null, null, null));
        assertNotNull(updated.getFinishedOn());
    }

    @Test
    void explicitDateInSamePatchOverridesAutoStamp() {
        Book book = create("Dune", "owned");
        Book updated = bookService.update(book.getId(), new BookUpdateRequest(
                null, null, "reading", "2020-01-01", null, null, null, null));
        assertEquals("2020-01-01", updated.getStartedOn());
    }

    @Test
    void deleteHidesFromListAndRestoreBringsBack() {
        Book book = create("Dune", "wishlist");
        bookService.delete(book.getId());
        List<Book> afterDelete = bookService.list();
        assertTrue(afterDelete.stream().noneMatch(b -> b.getId().equals(book.getId())));

        bookService.restore(book.getId());
        List<Book> afterRestore = bookService.list();
        assertTrue(afterRestore.stream().anyMatch(b -> b.getId().equals(book.getId())));
    }

    @Test
    void updatingDeletedBookThrows() {
        Book book = create("Dune", "wishlist");
        bookService.delete(book.getId());
        assertThrows(ResponseStatusException.class, () -> bookService.update(
                book.getId(), new BookUpdateRequest("New Title", null, null, null, null, null, null, null)));
    }

    @Test
    void blankTitleUpdateIsRejected() {
        Book book = create("Dune", "wishlist");
        assertThrows(ResponseStatusException.class, () -> bookService.update(
                book.getId(), new BookUpdateRequest("   ", null, null, null, null, null, null, null)));
    }
}
