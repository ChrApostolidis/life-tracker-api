package com.lifeTracker.life_tracker_api.sleepLogs;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SleepLogRepository extends JpaRepository<SleepLog, String> {

    // Includes soft-deleted rows on purpose — sleepDate is unique, so an upsert
    // has to revive a deleted night rather than insert a duplicate.
    Optional<SleepLog> findBySleepDate(String sleepDate);

    // sleepDate is 'YYYY-MM-DD', so string comparison is chronological.
    @Query("SELECT s FROM SleepLog s WHERE s.sleepDate >= :from AND s.sleepDate < :to AND s.deletedAt IS NULL ORDER BY s.sleepDate ASC")
    List<SleepLog> findLogsInRange(@Param("from") String from, @Param("to") String to);
}
