package com.lifeTracker.life_tracker_api.habits;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HabitRepository extends JpaRepository<Habit, String> {

    // Includes archived habits — the frontend shows them in an Archived
    // section rather than hiding them entirely, unlike soft-deleted rows
    // elsewhere in the app.
    List<Habit> findAllByOrderByCreatedAtAsc();
}
