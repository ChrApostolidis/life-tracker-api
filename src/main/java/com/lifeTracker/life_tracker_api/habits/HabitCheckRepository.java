package com.lifeTracker.life_tracker_api.habits;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HabitCheckRepository extends JpaRepository<HabitCheck, String> {

    // checkedOn is 'YYYY-MM-DD', so string comparison is chronological.
    @Query("SELECT c FROM HabitCheck c WHERE c.checkedOn >= :from AND c.checkedOn < :to ORDER BY c.checkedOn ASC")
    List<HabitCheck> findChecksInRange(@Param("from") String from, @Param("to") String to);

    Optional<HabitCheck> findByHabitIdAndCheckedOn(String habitId, String checkedOn);
}
