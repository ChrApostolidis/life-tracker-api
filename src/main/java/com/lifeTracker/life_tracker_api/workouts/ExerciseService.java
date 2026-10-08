package com.lifeTracker.life_tracker_api.workouts;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final WorkoutRepository workoutRepository;
    private final WorkoutSetRepository workoutSetRepository;
    private final GymSettingsRepository gymSettingsRepository;

    public ExerciseService(
            ExerciseRepository exerciseRepository,
            WorkoutRepository workoutRepository,
            WorkoutSetRepository workoutSetRepository,
            GymSettingsRepository gymSettingsRepository
    ) {
        this.exerciseRepository = exerciseRepository;
        this.workoutRepository = workoutRepository;
        this.workoutSetRepository = workoutSetRepository;
        this.gymSettingsRepository = gymSettingsRepository;
    }

    public List<Exercise> list() {
        String style = currentStyle();
        List<Exercise> exercises = exerciseRepository.findAllByOrderByNameAsc();
        exercises.forEach(exercise -> applyEffectiveRange(exercise, style));
        return exercises;
    }

    public Exercise create(ExerciseCreateRequest request) {
        if ((request.repMin() == null) != (request.repMax() == null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "send both repMin and repMax, or neither");
        }
        Exercise exercise = new Exercise();
        exercise.setId(UUID.randomUUID().toString());
        exercise.setName(request.name().trim());
        exercise.setMuscleGroup(request.muscleGroup());
        exercise.setEquipment(request.equipment());
        exercise.setKind(request.kind());
        if (request.repMin() != null) setOverride(exercise, request.repMin(), request.repMax());
        exercise.setWeightStepGrams(request.weightStepGrams() != null
                ? request.weightStepGrams()
                : defaultStepGrams(request.equipment()));
        exercise.setCreatedAt(Instant.now());
        exercise.setUpdatedAt(Instant.now());
        return withEffectiveRange(exerciseRepository.save(exercise));
    }

    public Exercise update(String id, ExerciseUpdateRequest request) {
        Exercise exercise = getExerciseOrThrow(id);
        if (request.name() != null) {
            if (request.name().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name must not be blank");
            }
            exercise.setName(request.name().trim());
        }
        if (request.muscleGroup() != null) exercise.setMuscleGroup(request.muscleGroup());
        if (request.equipment() != null) exercise.setEquipment(request.equipment());
        if (request.kind() != null) exercise.setKind(request.kind());
        if (request.weightStepGrams() != null) exercise.setWeightStepGrams(request.weightStepGrams());

        // Either bound pins the whole range: the end not sent keeps whatever is
        // in effect now, so editing just the top of a style range works.
        if (request.repMin() != null || request.repMax() != null) {
            applyEffectiveRange(exercise, currentStyle());
            setOverride(exercise,
                    request.repMin() != null ? request.repMin() : exercise.getRepMin(),
                    request.repMax() != null ? request.repMax() : exercise.getRepMax());
        }

        exercise.setUpdatedAt(Instant.now());
        return withEffectiveRange(exerciseRepository.save(exercise));
    }

    // Back to following the training style. Its own verb because PATCH reads null as "unchanged".
    public Exercise clearRepRange(String id) {
        Exercise exercise = getExerciseOrThrow(id);
        exercise.setRepMinOverride(null);
        exercise.setRepMaxOverride(null);
        exercise.setUpdatedAt(Instant.now());
        return withEffectiveRange(exerciseRepository.save(exercise));
    }

    public void archive(String id) {
        Exercise exercise = getExerciseOrThrow(id);
        exercise.setArchivedAt(Instant.now());
        exercise.setUpdatedAt(Instant.now());
        exerciseRepository.save(exercise);
    }

    public void unarchive(String id) {
        Exercise exercise = getExerciseOrThrow(id);
        exercise.setArchivedAt(null);
        exercise.setUpdatedAt(Instant.now());
        exerciseRepository.save(exercise);
    }

    // Past sessions of one exercise, newest first. excludeWorkoutId (nullable)
    // drops the workout in progress so it never counts as its own history.
    public List<ExerciseSession> history(String id, int limit, String excludeWorkoutId) {
        getExerciseOrThrow(id);
        if (limit < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "limit must be at least 1");
        }

        List<WorkoutSet> sets = workoutSetRepository.findHistoryForExercise(id);
        // Sets arrive newest workout first, so insertion order is session order.
        Map<String, List<WorkoutSet>> setsByWorkout = new LinkedHashMap<>();
        for (WorkoutSet set : sets) {
            if (set.getWorkoutId().equals(excludeWorkoutId)) continue;
            setsByWorkout.computeIfAbsent(set.getWorkoutId(), k -> new ArrayList<>()).add(set);
        }

        Map<String, Workout> workouts = workoutRepository.findAllById(setsByWorkout.keySet()).stream()
                .collect(Collectors.toMap(Workout::getId, Function.identity()));
        return setsByWorkout.entrySet().stream()
                .limit(limit)
                .map(e -> new ExerciseSession(e.getKey(), workouts.get(e.getKey()).getPerformedOn(), e.getValue()))
                .toList();
    }

    public ProgressionSuggestion suggestion(String id, String excludeWorkoutId) {
        Exercise exercise = withEffectiveRange(getExerciseOrThrow(id));
        // The deload rule looks back three sessions; the spares cover sessions
        // that turn out to be warm-up only.
        return Progression.suggest(exercise, history(id, 5, excludeWorkoutId));
    }

    public GymSettings getSettings() {
        return getSettingsRow();
    }

    public GymSettings updateSettings(GymSettingsUpdateRequest request) {
        GymSettings settings = getSettingsRow();
        settings.setTrainingStyle(request.trainingStyle());
        settings.setUpdatedAt(Instant.now());
        return gymSettingsRepository.save(settings);
    }

    private Exercise withEffectiveRange(Exercise exercise) {
        applyEffectiveRange(exercise, currentStyle());
        return exercise;
    }

    private static void applyEffectiveRange(Exercise exercise, String style) {
        if (exercise.getRepMinOverride() != null) {
            exercise.setRepMin(exercise.getRepMinOverride());
            exercise.setRepMax(exercise.getRepMaxOverride());
            return;
        }
        TrainingStyles.RepRange range = TrainingStyles.rangeFor(style, exercise.getKind());
        exercise.setRepMin(range.min());
        exercise.setRepMax(range.max());
    }

    private static void setOverride(Exercise exercise, int repMin, int repMax) {
        if (repMin > repMax) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "repMin must not exceed repMax");
        }
        exercise.setRepMinOverride(repMin);
        exercise.setRepMaxOverride(repMax);
    }

    private String currentStyle() {
        return getSettingsRow().getTrainingStyle();
    }

    // The migration seeds this row; recreating it keeps a hand-edited DB working.
    private GymSettings getSettingsRow() {
        return gymSettingsRepository.findById(GymSettings.ROW_ID).orElseGet(() -> {
            GymSettings settings = new GymSettings();
            settings.setId(GymSettings.ROW_ID);
            settings.setUpdatedAt(Instant.now());
            return gymSettingsRepository.save(settings);
        });
    }

    // The smallest jump a typical gym offers for each kind of kit.
    private static int defaultStepGrams(String equipment) {
        return switch (equipment) {
            case "barbell", "cable" -> 2500;
            case "dumbbell" -> 2000;
            case "machine" -> 5000;
            default -> 0; // bodyweight progresses by reps
        };
    }

    private Exercise getExerciseOrThrow(String id) {
        return exerciseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exercise not found"));
    }
}
