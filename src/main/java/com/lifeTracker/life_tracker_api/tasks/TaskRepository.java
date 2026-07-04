package com.lifeTracker.life_tracker_api.tasks;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, String> {

    List<Task> findByScheduledAtIsNullAndDeletedAtIsNull();

    @Query("SELECT t FROM Task t WHERE t.scheduledAt >= :from AND t.scheduledAt < :to AND t.deletedAt IS NULL ORDER BY t.scheduledAt ASC")
    List<Task> findTasksInRange(
            @Param("from") Instant from,
            @Param("to") Instant to
    );
}
