package com.lifeTracker.life_tracker_api.books;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:3000", "https://lifetracker.christosapostolidis.com"})
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/books")
    public ResponseEntity<List<Book>> listBooks() {
        return ResponseEntity.ok(bookService.list());
    }

    @PostMapping("/books")
    public ResponseEntity<Book> createBook(@Valid @RequestBody BookCreateRequest request) {
        return ResponseEntity.ok(bookService.create(request));
    }

    @PatchMapping("/books/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable String id, @Valid @RequestBody BookUpdateRequest request) {
        return ResponseEntity.ok(bookService.update(id, request));
    }

    @DeleteMapping("/books/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable String id) {
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/books/{id}/restore")
    public ResponseEntity<Void> restoreBook(@PathVariable String id) {
        bookService.restore(id);
        return ResponseEntity.noContent().build();
    }
}
