package com.lifeTracker.life_tracker_api.tasks;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TaskCompletionRepository extends JpaRepository<TaskCompletion, String> {

    List<TaskCompletion> findByTaskIdIn(List<String> taskIds);

    Optional<TaskCompletion> findByTaskIdAndOccurrenceDate(String taskId, String occurrenceDate);

    void deleteByTaskIdAndOccurrenceDate(String taskId, String occurrenceDate);
}
