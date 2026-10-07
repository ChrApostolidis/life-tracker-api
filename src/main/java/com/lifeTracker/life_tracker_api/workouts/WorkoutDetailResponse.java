package com.lifeTracker.life_tracker_api.workouts;

import java.util.List;

// A workout with its live sets, in position order — what the active-workout
// screen renders. The list endpoint returns bare workouts to stay light.
public record WorkoutDetailResponse(Workout workout, List<WorkoutSet> sets) {}
