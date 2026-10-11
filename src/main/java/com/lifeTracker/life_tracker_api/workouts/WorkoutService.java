package com.lifeTracker.life_tracker_api.workouts;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class WorkoutService {

    private final WorkoutRepository workoutRepository;
    private final WorkoutSetRepository workoutSetRepository;
    private final ExerciseRepository exerciseRepository;
    private final ZoneId zone;

    public WorkoutService(
            WorkoutRepository workoutRepository,
            WorkoutSetRepository workoutSetRepository,
            ExerciseRepository exerciseRepository,
            @Value("${app.timezone:Europe/Athens}") String timezoneId
    ) {
        this.workoutRepository = workoutRepository;
        this.workoutSetRepository = workoutSetRepository;
        this.exerciseRepository = exerciseRepository;
        this.zone = ZoneId.of(timezoneId);
    }

    public List<WorkoutSummaryResponse> list() {
        return summarize(workoutRepository.findByDeletedAtIsNullOrderByPerformedOnDescStartedAtDesc());
    }

    public List<WorkoutSummaryResponse> list(String from, String to) {
        validateDate(from, "from");
        validateDate(to, "to");
        return summarize(workoutRepository.findWorkoutsInRange(from, to));
    }

    // One query for every live set of these workouts, folded into a summary
    // each, so the history list never fetches workouts one by one.
    private List<WorkoutSummaryResponse> summarize(List<Workout> workouts) {
        List<String> ids = workouts.stream().map(Workout::getId).toList();
        Map<String, List<WorkoutSet>> setsByWorkout = workoutSetRepository
                .findByWorkoutIdInAndDeletedAtIsNullOrderByPositionAsc(ids).stream()
                .collect(Collectors.groupingBy(WorkoutSet::getWorkoutId));
        return workouts.stream()
                .map(w -> WorkoutSummaryResponse.of(w, setsByWorkout.getOrDefault(w.getId(), List.of())))
                .toList();
    }

    public WorkoutDetailResponse get(String id) {
        Workout workout = getLiveWorkoutOrThrow(id);
        return new WorkoutDetailResponse(workout, workoutSetRepository.findByWorkoutIdAndDeletedAtIsNullOrderByPositionAsc(id));
    }

    // A client-minted id that already exists means a retry: hand back the row
    // the first attempt created rather than a duplicate.
    public Workout create(WorkoutCreateRequest request) {
        if (request.id() != null) {
            Optional<Workout> existing = workoutRepository.findById(request.id());
            if (existing.isPresent()) return existing.get();
        }
        Workout workout = new Workout();
        workout.setId(request.id() != null ? request.id() : UUID.randomUUID().toString());
        workout.setPerformedOn(request.performedOn() != null ? request.performedOn() : LocalDate.now(zone).toString());
        workout.setStartedAt(request.startedAt() != null ? request.startedAt() : Instant.now());
        workout.setNotes(blankToNull(request.notes()));
        workout.setCreatedAt(Instant.now());
        workout.setUpdatedAt(Instant.now());
        return workoutRepository.save(workout);
    }

    public Workout update(String id, WorkoutUpdateRequest request) {
        Workout workout = getLiveWorkoutOrThrow(id);
        if (request.performedOn() != null) workout.setPerformedOn(request.performedOn());
        if (request.notes() != null) workout.setNotes(blankToNull(request.notes()));
        workout.setUpdatedAt(Instant.now());
        return workoutRepository.save(workout);
    }

    // Idempotent: finishing twice keeps the first finish time. finishedAt is
    // when Finish was tapped on an offline client; null = now.
    public Workout finish(String id, Instant finishedAt) {
        Workout workout = getLiveWorkoutOrThrow(id);
        if (finishedAt != null && finishedAt.isBefore(workout.getStartedAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "finishedAt is before startedAt");
        }
        if (workout.getFinishedAt() == null) {
            workout.setFinishedAt(finishedAt != null ? finishedAt : Instant.now());
            workout.setUpdatedAt(Instant.now());
            workoutRepository.save(workout);
        }
        return workout;
    }

    public void delete(String id) {
        Workout workout = getLiveWorkoutOrThrow(id);
        workout.setDeletedAt(Instant.now());
        workout.setUpdatedAt(Instant.now());
        workoutRepository.save(workout);
    }

    public void restore(String id) {
        Workout workout = getWorkoutOrThrow(id);
        workout.setDeletedAt(null);
        workout.setUpdatedAt(Instant.now());
        workoutRepository.save(workout);
    }

    public WorkoutSet addSet(String workoutId, WorkoutSetCreateRequest request) {
        getLiveWorkoutOrThrow(workoutId);
        // Same retry rule as create(): a known client id returns the first attempt's set.
        if (request.id() != null) {
            Optional<WorkoutSet> existing = workoutSetRepository.findById(request.id());
            if (existing.isPresent()) {
                if (!existing.get().getWorkoutId().equals(workoutId)) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Set id belongs to another workout");
                }
                return existing.get();
            }
        }
        // The exercise comes from the body, not the path, so unknown is bad input.
        if (!exerciseRepository.existsById(request.exerciseId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown exerciseId");
        }
        int nextPosition = workoutSetRepository.findTopByWorkoutIdOrderByPositionDesc(workoutId)
                .map(last -> last.getPosition() + 1)
                .orElse(0);

        WorkoutSet set = new WorkoutSet();
        set.setId(request.id() != null ? request.id() : UUID.randomUUID().toString());
        set.setWorkoutId(workoutId);
        set.setExerciseId(request.exerciseId());
        set.setPosition(nextPosition);
        set.setWeightGrams(request.weightGrams());
        set.setReps(request.reps());
        set.setWarmup(Boolean.TRUE.equals(request.warmup()));
        set.setCreatedAt(Instant.now());
        set.setUpdatedAt(Instant.now());
        return workoutSetRepository.save(set);
    }

    public WorkoutSet updateSet(String id, WorkoutSetUpdateRequest request) {
        WorkoutSet set = getLiveSetOrThrow(id);
        if (request.weightGrams() != null) set.setWeightGrams(request.weightGrams());
        if (request.reps() != null) set.setReps(request.reps());
        if (request.warmup() != null) set.setWarmup(request.warmup());
        set.setUpdatedAt(Instant.now());
        return workoutSetRepository.save(set);
    }

    public void deleteSet(String id) {
        WorkoutSet set = getLiveSetOrThrow(id);
        set.setDeletedAt(Instant.now());
        set.setUpdatedAt(Instant.now());
        workoutSetRepository.save(set);
    }

    public void restoreSet(String id) {
        WorkoutSet set = workoutSetRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Set not found"));
        set.setDeletedAt(null);
        set.setUpdatedAt(Instant.now());
        workoutSetRepository.save(set);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void validateDate(String value, String paramName) {
        try {
            LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'" + paramName + "' must be 'YYYY-MM-DD'");
        }
    }

    // Includes soft-deleted rows — only /restore should use this.
    private Workout getWorkoutOrThrow(String id) {
        return workoutRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workout not found"));
    }

    private Workout getLiveWorkoutOrThrow(String id) {
        Workout workout = getWorkoutOrThrow(id);
        if (workout.getDeletedAt() != null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Workout not found");
        }
        return workout;
    }

    private WorkoutSet getLiveSetOrThrow(String id) {
        return workoutSetRepository.findById(id)
                .filter(set -> set.getDeletedAt() == null)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Set not found"));
    }
}
