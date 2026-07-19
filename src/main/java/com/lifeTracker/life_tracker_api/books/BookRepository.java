package com.lifeTracker.life_tracker_api.books;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, String> {

    List<Book> findByDeletedAtIsNullOrderByCreatedAtDesc();
}
