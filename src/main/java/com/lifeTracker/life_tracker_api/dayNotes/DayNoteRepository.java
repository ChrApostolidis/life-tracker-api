package com.lifeTracker.life_tracker_api.dayNotes;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DayNoteRepository extends JpaRepository<DayNote, String> {

    // Includes soft-deleted rows on purpose — entryDate is unique, so an
    // upsert has to find and revive a cleared entry rather than insert a
    // duplicate and hit the unique index.
    Optional<DayNote> findByEntryDate(String entryDate);

    // entryDate is 'YYYY-MM-DD', so string comparison is chronological.
    @Query("SELECT d FROM DayNote d WHERE d.entryDate >= :from AND d.entryDate < :to AND d.deletedAt IS NULL ORDER BY d.entryDate ASC")
    List<DayNote> findEntriesInRange(@Param("from") String from, @Param("to") String to);
}
