package com.lifeTracker.life_tracker_api.books;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final ZoneId zone;

    public BookService(
            BookRepository bookRepository,
            @Value("${app.timezone:Europe/Athens}") String timezoneId
    ) {
        this.bookRepository = bookRepository;
        this.zone = ZoneId.of(timezoneId);
    }

    public List<Book> list() {
        return bookRepository.findByDeletedAtIsNullOrderByCreatedAtDesc();
    }

    public Book create(BookCreateRequest request) {
        Book book = new Book();
        book.setId(UUID.randomUUID().toString());
        book.setTitle(request.title());
        book.setAuthor(request.author());
        book.setStatus(request.status());
        book.setCoverUrl(request.coverUrl());
        book.setCreatedAt(Instant.now());
        book.setUpdatedAt(Instant.now());
        return bookRepository.save(book);
    }

    public Book update(String id, BookUpdateRequest request) {
        Book book = getBookOrThrow(id);
        if (book.getDeletedAt() != null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found");
        }

        if (request.title() != null) {
            if (request.title().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title must not be blank");
            }
            book.setTitle(request.title());
        }
        if (request.author() != null) book.setAuthor(request.author());
        if (request.status() != null) {
            book.setStatus(request.status());
            // Auto-stamp only when the field is still empty — an explicit date
            // in the same request (handled below) always wins.
            if ("reading".equals(request.status()) && book.getStartedOn() == null) {
                book.setStartedOn(today());
            }
            if ("finished".equals(request.status()) && book.getFinishedOn() == null) {
                book.setFinishedOn(today());
            }
        }
        if (request.startedOn() != null) book.setStartedOn(request.startedOn());
        if (request.finishedOn() != null) book.setFinishedOn(request.finishedOn());
        if (request.rating() != null) book.setRating(request.rating());
        if (request.notes() != null) book.setNotes(request.notes());
        if (request.coverUrl() != null) book.setCoverUrl(request.coverUrl());

        book.setUpdatedAt(Instant.now());
        return bookRepository.save(book);
    }

    public void delete(String id) {
        Book book = getBookOrThrow(id);
        book.setDeletedAt(Instant.now());
        book.setUpdatedAt(Instant.now());
        bookRepository.save(book);
    }

    public void restore(String id) {
        Book book = getBookOrThrow(id);
        book.setDeletedAt(null);
        book.setUpdatedAt(Instant.now());
        bookRepository.save(book);
    }

    private String today() {
        return LocalDate.now(zone).toString();
    }

    private Book getBookOrThrow(String id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));
    }
}
