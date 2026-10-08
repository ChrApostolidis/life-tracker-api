package com.lifeTracker.life_tracker_api.workouts;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, String> {

    // Archived rows included — the picker hides them, but history still names them.
    List<Exercise> findAllByOrderByNameAsc();
}
