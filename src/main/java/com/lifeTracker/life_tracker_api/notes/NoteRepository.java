package com.lifeTracker.life_tracker_api.notes;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, String> {

    List<Note> findByTaskIdIsNullAndBookIdIsNullAndDeletedAtIsNullOrderByCreatedAtDesc();

    List<Note> findByBookIdAndDeletedAtIsNullOrderByCreatedAtDesc(String bookId);
}
