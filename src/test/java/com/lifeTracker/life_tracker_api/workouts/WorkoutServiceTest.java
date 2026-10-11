package com.lifeTracker.life_tracker_api.workouts;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Each test rolls back at the end (Spring test transaction), so tests can
// freely log workouts against the shared test DB without cleaning up.
@SpringBootTest
@Transactional
class WorkoutServiceTest {

    private static final String BENCH = "seed-barbell-bench-press";

    @Autowired
    private WorkoutService workoutService;

    @Autowired
    private ExerciseService exerciseService;

    private Workout workoutOn(String date) {
        return workoutService.create(new WorkoutCreateRequest(date, null, null, null));
    }

    private WorkoutSet log(Workout workout, int weightGrams, int reps) {
        return workoutService.addSet(workout.getId(), new WorkoutSetCreateRequest(BENCH, weightGrams, reps, null, null));
    }

    @Test
    void migrationSeedsTheExerciseLibrary() {
        List<Exercise> exercises = exerciseService.list();
        assertTrue(exercises.stream().anyMatch(e -> e.getId().equals(BENCH)));
    }

    @Test
    void setsGetIncreasingPositionsEvenAfterADelete() {
        Workout workout = workoutOn("2026-10-01");
        WorkoutSet first = log(workout, 60000, 10);
        WorkoutSet second = log(workout, 60000, 9);
        workoutService.deleteSet(second.getId());
        WorkoutSet third = log(workout, 60000, 8);

        assertEquals(0, first.getPosition());
        assertEquals(2, third.getPosition());
        assertEquals(2, workoutService.get(workout.getId()).sets().size());
    }

    @Test
    void suggestionReadsThePreviousSessionAndIgnoresTheCurrentOne() {
        Workout last = workoutOn("2026-10-01");
        log(last, 60000, 12);
        log(last, 60000, 12);
        workoutService.finish(last.getId(), null);

        // The half-done session in progress would otherwise be judged as "last time".
        Workout current = workoutOn("2026-10-04");
        log(current, 62500, 5);

        ProgressionSuggestion s = exerciseService.suggestion(BENCH, current.getId());
        assertEquals("increase", s.action());
        assertEquals(62500, s.weightGrams());
        assertEquals(last.getId(), s.basedOn().workoutId());
    }

    @Test
    void deletedWorkoutsDropOutOfHistory() {
        Workout gone = workoutOn("2026-10-02");
        log(gone, 60000, 12);
        workoutService.delete(gone.getId());

        assertTrue(exerciseService.history(BENCH, 10, null).stream()
                .noneMatch(session -> session.workoutId().equals(gone.getId())));
    }

    @Test
    void listSummarizesEachWorkoutWithoutWarmups() {
        Workout workout = workoutOn("2026-10-03");
        workoutService.addSet(workout.getId(), new WorkoutSetCreateRequest(BENCH, 20000, 10, true, null));
        log(workout, 60000, 10);
        workoutService.addSet(workout.getId(), new WorkoutSetCreateRequest("seed-barbell-row", 50000, 8, null, null));
        WorkoutSet deleted = log(workout, 60000, 9);
        workoutService.deleteSet(deleted.getId());

        WorkoutSummaryResponse summary = workoutService.list("2026-10-03", "2026-10-04").stream()
                .filter(s -> s.workout().getId().equals(workout.getId()))
                .findFirst().orElseThrow();
        assertEquals(List.of(BENCH, "seed-barbell-row"), summary.exerciseIds());
        assertEquals(2, summary.workingSetCount());
        assertEquals(60000L * 10 + 50000L * 8, summary.volumeGrams());
    }

    @Test
    void finishingTwiceKeepsTheFirstFinishTime() {
        Workout workout = workoutOn("2026-10-01");
        Workout finished = workoutService.finish(workout.getId(), null);
        assertNotNull(finished.getFinishedAt());
        assertEquals(finished.getFinishedAt(), workoutService.finish(workout.getId(), null).getFinishedAt());
    }

    @Test
    void retriedCreatesWithAClientIdReturnTheFirstRow() {
        Instant startedAt = Instant.parse("2026-10-05T15:00:00Z");
        WorkoutCreateRequest request = new WorkoutCreateRequest("2026-10-05", null, "client-workout-1", startedAt);
        Workout first = workoutService.create(request);
        Workout retry = workoutService.create(request);
        assertEquals("client-workout-1", first.getId());
        assertEquals(startedAt, first.getStartedAt());
        assertEquals(first.getCreatedAt(), retry.getCreatedAt());

        WorkoutSetCreateRequest set = new WorkoutSetCreateRequest(BENCH, 60000, 10, null, "client-set-1");
        workoutService.addSet(first.getId(), set);
        workoutService.addSet(first.getId(), set);
        assertEquals(1, workoutService.get(first.getId()).sets().size());
    }

    @Test
    void aClientSetIdCannotMoveToAnotherWorkout() {
        Workout one = workoutOn("2026-10-05");
        Workout two = workoutOn("2026-10-06");
        WorkoutSetCreateRequest set = new WorkoutSetCreateRequest(BENCH, 60000, 10, null, "client-set-2");
        workoutService.addSet(one.getId(), set);
        assertThrows(ResponseStatusException.class, () -> workoutService.addSet(two.getId(), set));
    }

    @Test
    void finishKeepsAnOfflineClientsFinishTime() {
        Instant startedAt = Instant.parse("2026-10-05T15:00:00Z");
        Workout workout = workoutService.create(new WorkoutCreateRequest("2026-10-05", null, null, startedAt));
        Instant finishedAt = Instant.parse("2026-10-05T16:10:00Z");
        assertEquals(finishedAt, workoutService.finish(workout.getId(), finishedAt).getFinishedAt());
        assertThrows(ResponseStatusException.class, () -> workoutService.finish(
                workout.getId(), Instant.parse("2026-10-05T14:00:00Z")));
    }

    @Test
    void unknownExerciseIsRejected() {
        Workout workout = workoutOn("2026-10-01");
        assertThrows(ResponseStatusException.class, () -> workoutService.addSet(
                workout.getId(), new WorkoutSetCreateRequest("no-such-exercise", 60000, 10, null, null)));
    }

    @Test
    void stepDefaultsFromEquipmentAndRepRangeIsValidated() {
        Exercise created = exerciseService.create(new ExerciseCreateRequest("Goblet Squat", "legs", "dumbbell", "compound", 8, 12, null));
        assertEquals(2000, created.getWeightStepGrams());

        // A PATCH that sends only one end is checked against the stored other end.
        assertThrows(ResponseStatusException.class, () -> exerciseService.update(
                created.getId(), new ExerciseUpdateRequest(null, null, null, null, 13, null, null)));
    }

    private Exercise find(String id) {
        return exerciseService.list().stream().filter(e -> e.getId().equals(id)).findFirst().orElseThrow();
    }

    private void chooseStyle(String style) {
        exerciseService.updateSettings(new GymSettingsUpdateRequest(style));
    }

    @Test
    void noStyleChosenFallsBackToHypertrophyRanges() {
        assertNull(exerciseService.getSettings().getTrainingStyle());
        assertEquals(8, find(BENCH).getRepMin());
        assertEquals(12, find(BENCH).getRepMax());
        assertEquals(10, find("seed-lateral-raise").getRepMin());
    }

    @Test
    void theStyleMovesCompoundsFurtherThanIsolations() {
        chooseStyle("balanced");
        assertEquals(5, find(BENCH).getRepMin());
        assertEquals(8, find(BENCH).getRepMax());
        assertEquals(8, find("seed-lateral-raise").getRepMin());
        assertEquals(12, find("seed-lateral-raise").getRepMax());
    }

    @Test
    void anOverrideWinsOverTheStyleUntilCleared() {
        chooseStyle("strength");
        // Seeded bodyweight moves are pinned: a 3-rep push-up target means nothing.
        assertEquals(10, find("seed-push-up").getRepMin());

        // Sending only the top pins the whole range, keeping the style's bottom.
        Exercise pinned = exerciseService.update(BENCH, new ExerciseUpdateRequest(null, null, null, null, null, 5, null));
        assertEquals(3, pinned.getRepMin());
        assertEquals(5, pinned.getRepMax());

        chooseStyle("hypertrophy");
        assertEquals(5, find(BENCH).getRepMax());

        Exercise cleared = exerciseService.clearRepRange(BENCH);
        assertNull(cleared.getRepMaxOverride());
        assertEquals(12, cleared.getRepMax());
    }

    @Test
    void creatingWithOnlyOneRepBoundIsRejected() {
        assertThrows(ResponseStatusException.class, () -> exerciseService.create(
                new ExerciseCreateRequest("Goblet Squat", "legs", "dumbbell", "compound", 8, null, null)));
    }

    @Test
    void theSuggestionUsesTheStyleRange() {
        // 3x8 at 60 kg is the top of the balanced 5-8 range, but mid-range for hypertrophy.
        Workout last = workoutOn("2026-10-01");
        log(last, 60000, 8);
        log(last, 60000, 8);
        log(last, 60000, 8);

        assertEquals("repeat", exerciseService.suggestion(BENCH, null).action());

        chooseStyle("balanced");
        ProgressionSuggestion s = exerciseService.suggestion(BENCH, null);
        assertEquals("increase", s.action());
        assertEquals(62500, s.weightGrams());
        assertEquals(5, s.targetReps());
    }
}
