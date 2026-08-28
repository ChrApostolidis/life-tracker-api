package com.lifeTracker.life_tracker_api.journalEntries;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, String> {

    // Newest day first, and within a day the most recently written first —
    // several entries can share an entryDate, so createdAt is the tiebreak.
    List<JournalEntry> findByDeletedAtIsNullOrderByEntryDateDescCreatedAtDesc();

    // entryDate is 'YYYY-MM-DD', so string comparison is chronological.
    @Query("SELECT j FROM JournalEntry j WHERE j.entryDate >= :from AND j.entryDate < :to AND j.deletedAt IS NULL ORDER BY j.entryDate DESC, j.createdAt DESC")
    List<JournalEntry> findEntriesInRange(@Param("from") String from, @Param("to") String to);
}
