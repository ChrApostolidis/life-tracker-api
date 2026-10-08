package com.lifeTracker.life_tracker_api.workouts;

import java.util.List;

// What a history card shows: the workout plus a digest of its sets, so the
// list endpoint never has to ship every set.
//
// exerciseIds are distinct, in the order first logged. workingSetCount and
// volumeGrams (sum of weight × reps, in gram-reps) skip warm-ups.
public record WorkoutSummaryResponse(
        Workout workout,
        List<String> exerciseIds,
        int workingSetCount,
        long volumeGrams
) {

    /** {@code sets} must be in position order. */
    static WorkoutSummaryResponse of(Workout workout, List<WorkoutSet> sets) {
        List<String> exerciseIds = sets.stream().map(WorkoutSet::getExerciseId).distinct().toList();
        List<WorkoutSet> working = sets.stream().filter(set -> !Boolean.TRUE.equals(set.getWarmup())).toList();
        long volume = working.stream().mapToLong(set -> (long) set.getWeightGrams() * set.getReps()).sum();
        return new WorkoutSummaryResponse(workout, exerciseIds, working.size(), volume);
    }
}
