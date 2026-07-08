package com.lifeTracker.life_tracker_api.money;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MoneyEntryRepository extends JpaRepository<MoneyEntry, String> {

    // occurredOn is 'YYYY-MM-DD', so string comparison is chronological.
    @Query("SELECT m FROM MoneyEntry m WHERE m.occurredOn >= :from AND m.occurredOn < :to AND m.deletedAt IS NULL ORDER BY m.occurredOn ASC, m.createdAt ASC")
    List<MoneyEntry> findEntriesInRange(
            @Param("from") String from,
            @Param("to") String to
    );

    @Query("SELECT COALESCE(SUM(m.amountCents), 0) FROM MoneyEntry m WHERE m.type = :type AND m.deletedAt IS NULL")
    long sumAmountCentsByType(@Param("type") String type);
}
