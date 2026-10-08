package com.lifeTracker.life_tracker_api.workouts;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkoutRepository extends JpaRepository<Workout, String> {

    List<Workout> findByDeletedAtIsNullOrderByPerformedOnDescStartedAtDesc();

    // performedOn is 'YYYY-MM-DD', so string comparison is chronological.
    @Query("SELECT w FROM Workout w WHERE w.performedOn >= :from AND w.performedOn < :to AND w.deletedAt IS NULL ORDER BY w.performedOn DESC, w.startedAt DESC")
    List<Workout> findWorkoutsInRange(@Param("from") String from, @Param("to") String to);
}
