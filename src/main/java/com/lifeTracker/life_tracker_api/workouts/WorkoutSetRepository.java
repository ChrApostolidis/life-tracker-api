package com.lifeTracker.life_tracker_api.workouts;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkoutSetRepository extends JpaRepository<WorkoutSet, String> {

    List<WorkoutSet> findByWorkoutIdAndDeletedAtIsNullOrderByPositionAsc(String workoutId);

    List<WorkoutSet> findByWorkoutIdInAndDeletedAtIsNullOrderByPositionAsc(Collection<String> workoutIds);

    // Includes soft-deleted sets, so a deleted set's position is never reused.
    Optional<WorkoutSet> findTopByWorkoutIdOrderByPositionDesc(String workoutId);

    // Every live set of one exercise across live workouts, newest workout
    // first and in position order within each, ready to group into sessions.
    @Query("SELECT s FROM WorkoutSet s, Workout w WHERE s.workoutId = w.id AND s.exerciseId = :exerciseId "
            + "AND s.deletedAt IS NULL AND w.deletedAt IS NULL "
            + "ORDER BY w.performedOn DESC, w.startedAt DESC, s.position ASC")
    List<WorkoutSet> findHistoryForExercise(@Param("exerciseId") String exerciseId);
}
