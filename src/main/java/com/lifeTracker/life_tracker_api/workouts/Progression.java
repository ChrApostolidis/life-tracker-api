package com.lifeTracker.life_tracker_api.workouts;

import java.util.List;

/**
 * Double progression: work up the rep range at one weight, and once every set
 * reaches the top of it, add a step and drop back to the bottom. Pure and
 * deterministic so it can be tested without Spring — see the web repo's
 * GYM_TRACKER_PLAN.md for the rules table.
 */
final class Progression {

    private static final double DELOAD_FACTOR = 0.9;

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
        if (lowestReps >= repMax) {
            return new ProgressionSuggestion("increase", weight + step, repMin, top.size(), last);
        }
        if (missedRange(last, repMin) && judged.size() > 1
                && missedRange(judged.get(1), repMin) && workingWeight(judged.get(1)) == weight) {
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

    private static int workingWeight(ExerciseSession session) {
        return topSets(session).get(0).getWeightGrams();
    }

    private static int lowestReps(List<WorkoutSet> sets) {
        return sets.stream().mapToInt(WorkoutSet::getReps).min().orElse(0);
    }

    private static boolean missedRange(ExerciseSession session, int repMin) {
        return lowestReps(topSets(session)) < repMin;
    }

    // ~90%, rounded DOWN to the step: a deload should never round toward heavier,
    // and flooring 90% always lands at least one step below the current weight.
    private static int deloadWeight(int weight, int step) {
        return (int) Math.floor(weight * DELOAD_FACTOR / step) * step;
    }
}
