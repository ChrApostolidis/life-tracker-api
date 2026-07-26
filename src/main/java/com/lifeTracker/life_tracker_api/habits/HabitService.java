package com.lifeTracker.life_tracker_api.habits;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class HabitService {

    private static final Pattern DATE_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");

    private final HabitRepository habitRepository;
    private final HabitCheckRepository habitCheckRepository;

    public HabitService(HabitRepository habitRepository, HabitCheckRepository habitCheckRepository) {
        this.habitRepository = habitRepository;
        this.habitCheckRepository = habitCheckRepository;
    }

    // All habits, including archived — the frontend splits them into an
    // active checklist and an Archived section.
    public List<Habit> list() {
        return habitRepository.findAllByOrderByCreatedAtAsc();
    }

    public Habit create(HabitCreateRequest request) {
        Habit habit = new Habit();
        habit.setId(UUID.randomUUID().toString());
        habit.setName(request.name());
        habit.setCreatedAt(Instant.now());
        habit.setUpdatedAt(Instant.now());
        return habitRepository.save(habit);
    }

    public Habit update(String id, HabitUpdateRequest request) {
        Habit habit = getHabitOrThrow(id);
        if (request.name() != null) {
            if (request.name().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name must not be blank");
            }
            habit.setName(request.name());
        }
        habit.setUpdatedAt(Instant.now());
        return habitRepository.save(habit);
    }

    public void archive(String id) {
        Habit habit = getHabitOrThrow(id);
        habit.setArchivedAt(Instant.now());
        habit.setUpdatedAt(Instant.now());
        habitRepository.save(habit);
    }

    public void unarchive(String id) {
        Habit habit = getHabitOrThrow(id);
        habit.setArchivedAt(null);
        habit.setUpdatedAt(Instant.now());
        habitRepository.save(habit);
    }

    public List<HabitCheck> listChecks(String from, String to) {
        return habitCheckRepository.findChecksInRange(from, to);
    }

    // Idempotent: checking an already-checked day just returns the existing row.
    public HabitCheck check(String habitId, String date) {
        getHabitOrThrow(habitId);
        if (!DATE_PATTERN.matcher(date).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "date must be YYYY-MM-DD");
        }
        return habitCheckRepository.findByHabitIdAndCheckedOn(habitId, date)
                .orElseGet(() -> {
                    HabitCheck check = new HabitCheck();
                    check.setId(UUID.randomUUID().toString());
                    check.setHabitId(habitId);
                    check.setCheckedOn(date);
                    check.setCreatedAt(Instant.now());
                    return habitCheckRepository.save(check);
                });
    }

    // Hard delete, not a soft delete — see the note on HabitCheck. No-op
    // (still succeeds) if the day was never checked.
    public void uncheck(String habitId, String date) {
        habitCheckRepository.findByHabitIdAndCheckedOn(habitId, date)
                .ifPresent(habitCheckRepository::delete);
    }

    private Habit getHabitOrThrow(String id) {
        return habitRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Habit not found"));
    }
}
