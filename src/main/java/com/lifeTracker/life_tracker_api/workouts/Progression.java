package com.lifeTracker.life_tracker_api.workouts;

import java.util.List;

/**
 * Double progression: work up the rep range at one weight, and once the sets
 * reach the top of it, add a step and drop back to the bottom. Pure and
 * deterministic so it can be tested without Spring — see the web repo's
 * GYM_TRACKER_PLAN.md for the rules table.
 */
final class Progression {

    private static final double DELOAD_FACTOR = 0.9;

    // Reps the top sets may fall short of repMax, in total, and still count as
    // "reached the top". Without it 12/12/11 would hold the weight another session.
    private static final int REP_TOLERANCE = 1;

    // Sessions in a row below repMin at the same weight before a deload.
    private static final int STRIKES_BEFORE_DELOAD = 3;

    // A step bigger than this share of the current weight (6 kg -> 8 kg dumbbells
    // is 33%) is only taken once the reps earn it — see earnedBigJump.
    private static final double MAX_PLAIN_JUMP = 0.10;

    private Progression() {}

    /** {@code sessions} must be newest first and exclude the workout in progress. */
    static ProgressionSuggestion suggest(Exercise exercise, List<ExerciseSession> sessions) {
        int repMin = exercise.getRepMin();
        int repMax = exercise.getRepMax();
        int step = exercise.getWeightStepGrams();

        // A session of nothing but warm-ups says nothing about working strength.
        List<ExerciseSession> judged = sessions.stream().filter(s -> !topSets(s).isEmpty()).toList();
        if (judged.isEmpty()) {
            return new ProgressionSuggestion("start", null, repMin, null, null);
        }

        ExerciseSession last = judged.get(0);
        List<WorkoutSet> top = topSets(last);
        int weight = top.get(0).getWeightGrams();
        int lowestReps = lowestReps(top);

        // Bodyweight: there is no step to add, so the range is open-ended.
        if (step == 0) {
            return new ProgressionSuggestion("repeat", weight, lowestReps + 1, top.size(), last);
        }
        if (repShortfall(top, repMax) <= REP_TOLERANCE) {
            if (step <= weight * MAX_PLAIN_JUMP || earnedBigJump(weight, lowestReps, step, repMin)) {
                return new ProgressionSuggestion("increase", weight + step, repMin, top.size(), last);
            }
            // Top of the range, but the next step is too big a leap yet: keep
            // the weight and go past repMax until the reps carry the jump.
            return new ProgressionSuggestion("extend", weight, lowestReps + 1, top.size(), last);
        }
        if (missStreakAt(judged, weight, repMin) >= STRIKES_BEFORE_DELOAD) {
            return new ProgressionSuggestion("deload", deloadWeight(weight, step), repMin, top.size(), last);
        }
        int target = Math.min(repMax, Math.max(repMin, lowestReps + 1));
        return new ProgressionSuggestion("repeat", weight, target, top.size(), last);
    }

    // The working sets at the session's heaviest working weight. Judging only
    // these keeps lighter back-off sets from dragging the verdict down.
    static List<WorkoutSet> topSets(ExerciseSession session) {
        List<WorkoutSet> working = session.sets().stream()
                .filter(set -> !Boolean.TRUE.equals(set.getWarmup()))
                .toList();
        int heaviest = working.stream().mapToInt(WorkoutSet::getWeightGrams).max().orElse(-1);
        return working.stream().filter(set -> set.getWeightGrams() == heaviest).toList();
    }

    // Take a big step only once the estimated max at today's weight covers
    // doing repMin at the heavier one. Epley runs high past ~10 reps, which
    // only makes the jump come slightly sooner.
    private static boolean earnedBigJump(int weight, int reps, int step, int repMin) {
        return epley(weight, reps) >= epley(weight + step, repMin);
    }

    private static double epley(int weightGrams, int reps) {
        return weightGrams * (1 + reps / 30.0);
    }

    private static int repShortfall(List<WorkoutSet> sets, int repMax) {
        return sets.stream().mapToInt(set -> Math.max(0, repMax - set.getReps())).sum();
    }

    // How many sessions in a row, newest first, missed repMin at this weight.
    private static int missStreakAt(List<ExerciseSession> judged, int weight, int repMin) {
        int streak = 0;
        for (ExerciseSession session : judged) {
            List<WorkoutSet> top = topSets(session);
            if (top.get(0).getWeightGrams() != weight || lowestReps(top) >= repMin) break;
            streak++;
        }
        return streak;
    }

    private static int lowestReps(List<WorkoutSet> sets) {
        return sets.stream().mapToInt(WorkoutSet::getReps).min().orElse(0);
    }

    // ~90%, rounded DOWN to the step: a deload should never round toward heavier,
    // and flooring 90% always lands at least one step below the current weight.
    private static int deloadWeight(int weight, int step) {
        return (int) Math.floor(weight * DELOAD_FACTOR / step) * step;
    }
}
