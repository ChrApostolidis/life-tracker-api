package com.lifeTracker.life_tracker_api.workouts;

import java.util.List;

// Every set of one exercise within one workout, warm-ups included, in position
// order. The unit both the history endpoint and Progression work in.
public record ExerciseSession(String workoutId, String performedOn, List<WorkoutSet> sets) {}
