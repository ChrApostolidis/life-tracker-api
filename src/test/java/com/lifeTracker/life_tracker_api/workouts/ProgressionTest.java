package com.lifeTracker.life_tracker_api.workouts;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

// Pure rules, no Spring: sessions are built in memory, newest first.
class ProgressionTest {

    private static Exercise exercise(int repMin, int repMax, int stepGrams) {
        Exercise exercise = new Exercise();
        exercise.setRepMin(repMin);
        exercise.setRepMax(repMax);
        exercise.setWeightStepGrams(stepGrams);
        return exercise;
    }

    private static final Exercise BENCH = exercise(8, 12, 2500);

    private static WorkoutSet set(int weightGrams, int reps, boolean warmup) {
        WorkoutSet set = new WorkoutSet();
        set.setWeightGrams(weightGrams);
        set.setReps(reps);
        set.setWarmup(warmup);
        return set;
    }

    // Working sets at one weight, e.g. session(60000, 12, 12, 12).
    private static ExerciseSession session(int weightGrams, int... reps) {
        List<WorkoutSet> sets = new ArrayList<>();
        for (int r : reps) sets.add(set(weightGrams, r, false));
        return new ExerciseSession("w", "2026-10-01", sets);
    }

    @Test
    void noHistoryAsksYouToPickAStartingWeight() {
        ProgressionSuggestion s = Progression.suggest(BENCH, List.of());
        assertEquals("start", s.action());
        assertNull(s.weightGrams());
        assertEquals(8, s.targetReps());
    }

    @Test
    void everySetAtTheTopOfTheRangeAddsAStep() {
        ProgressionSuggestion s = Progression.suggest(BENCH, List.of(session(60000, 12, 12, 12)));
        assertEquals("increase", s.action());
        assertEquals(62500, s.weightGrams());
        assertEquals(8, s.targetReps());
        assertEquals(3, s.setCount());
    }

    @Test
    void oneSetShortOfTheTopRepeatsTheWeightForOneMoreRep() {
        ProgressionSuggestion s = Progression.suggest(BENCH, List.of(session(60000, 12, 10, 9)));
        assertEquals("repeat", s.action());
        assertEquals(60000, s.weightGrams());
        assertEquals(10, s.targetReps());
    }

    @Test
    void targetRepsNeverExceedTheTopOfTheRange() {
        ProgressionSuggestion s = Progression.suggest(BENCH, List.of(session(60000, 12, 11, 11)));
        assertEquals("repeat", s.action());
        assertEquals(12, s.targetReps());
    }

    @Test
    void oneRepShortOfTheTopStillCountsAsReachingIt() {
        // 12/12/11 is one rep short in total — within tolerance, so the weight goes up.
        ProgressionSuggestion s = Progression.suggest(BENCH, List.of(session(60000, 12, 12, 11)));
        assertEquals("increase", s.action());
        assertEquals(62500, s.weightGrams());
    }

    @Test
    void oneMissedSessionRepeatsAtTheBottomOfTheRange() {
        ProgressionSuggestion s = Progression.suggest(BENCH, List.of(session(62500, 7, 6, 6), session(60000, 12, 12, 12)));
        assertEquals("repeat", s.action());
        assertEquals(62500, s.weightGrams());
        assertEquals(8, s.targetReps());
    }

    @Test
    void twoMissedSessionsAreNotEnoughToDeload() {
        ProgressionSuggestion s = Progression.suggest(BENCH, List.of(session(62500, 7, 6, 6), session(62500, 7, 7, 6)));
        assertEquals("repeat", s.action());
        assertEquals(62500, s.weightGrams());
    }

    @Test
    void threeMissedSessionsAtTheSameWeightDeload() {
        // 90% of 62.5 kg is 56.25 kg, exactly between steps — rounded down, never up.
        ProgressionSuggestion s = Progression.suggest(BENCH, List.of(
                session(62500, 7, 6, 6), session(62500, 7, 7, 6), session(62500, 7, 6, 5)));
        assertEquals("deload", s.action());
        assertEquals(55000, s.weightGrams());
        assertEquals(8, s.targetReps());
    }

    @Test
    void aGoodSessionInBetweenResetsTheStrikes() {
        ProgressionSuggestion s = Progression.suggest(BENCH, List.of(
                session(62500, 7, 6, 6), session(62500, 7, 7, 6), session(62500, 9, 8, 8), session(62500, 7, 6, 5)));
        assertEquals("repeat", s.action());
    }

    @Test
    void missesAtDifferentWeightsDoNotDeload() {
        ProgressionSuggestion s = Progression.suggest(BENCH, List.of(session(62500, 7, 6, 6), session(65000, 6, 6, 5)));
        assertEquals("repeat", s.action());
    }

    @Test
    void deloadIsAlwaysAtLeastOneStepLighter() {
        // 90% of 10 kg is 9 kg, which on a 5 kg step has to become 5 kg, not stay at 10.
        Exercise machine = exercise(8, 12, 5000);
        ProgressionSuggestion s = Progression.suggest(machine, List.of(
                session(10000, 5, 5), session(10000, 6, 5), session(10000, 5, 4)));
        assertEquals(5000, s.weightGrams());
    }

    @Test
    void aBigJumpWaitsWhileTheRepsCannotCarryIt() {
        // Lateral raise 6 kg -> 8 kg is a 33% jump. 15 reps at 6 kg estimates
        // short of 10 reps at 8 kg, so keep 6 kg and go past the range.
        Exercise lateralRaise = exercise(10, 15, 2000);
        ProgressionSuggestion s = Progression.suggest(lateralRaise, List.of(session(6000, 15, 15, 15)));
        assertEquals("extend", s.action());
        assertEquals(6000, s.weightGrams());
        assertEquals(16, s.targetReps());
    }

    @Test
    void aBigJumpIsTakenOnceTheRepsCarryIt() {
        // Leg extension 40 kg -> 45 kg is 12.5%. By Epley, 15 reps at 40 kg
        // outweighs 8 reps at 45 kg, so the jump is earned at the top of the range.
        Exercise legExtension = exercise(8, 15, 5000);
        ProgressionSuggestion s = Progression.suggest(legExtension, List.of(session(40000, 15, 15, 15)));
        assertEquals("increase", s.action());
        assertEquals(45000, s.weightGrams());
        assertEquals(8, s.targetReps());
    }

    @Test
    void warmupsAndBackOffSetsAreIgnored() {
        // Top sets hit 12 at 60 kg; the warm-up and the lighter back-off set with
        // fewer reps must not stop the increase.
        List<WorkoutSet> sets = List.of(
                set(20000, 5, true),
                set(60000, 12, false),
                set(60000, 12, false),
                set(50000, 8, false));
        ProgressionSuggestion s = Progression.suggest(BENCH, List.of(new ExerciseSession("w", "2026-10-01", sets)));
        assertEquals("increase", s.action());
        assertEquals(62500, s.weightGrams());
        assertEquals(2, s.setCount());
    }

    @Test
    void aWarmupOnlySessionIsSkipped() {
        ExerciseSession warmupOnly = new ExerciseSession("w2", "2026-10-03", List.of(set(20000, 10, true)));
        ProgressionSuggestion s = Progression.suggest(BENCH, List.of(warmupOnly, session(60000, 12, 12, 12)));
        assertEquals("increase", s.action());
    }

    @Test
    void bodyweightProgressesByRepsWithNoCap() {
        Exercise pullUp = exercise(5, 12, 0);
        ProgressionSuggestion s = Progression.suggest(pullUp, List.of(session(0, 12, 12, 12)));
        assertEquals("repeat", s.action());
        assertEquals(0, s.weightGrams());
        assertEquals(13, s.targetReps());
    }
}
