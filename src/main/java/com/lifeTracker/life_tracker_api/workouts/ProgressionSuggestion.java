package com.lifeTracker.life_tracker_api.workouts;

// What to try next time. Structured on purpose: the frontend phrases the
// "why" line from basedOn, so no UI copy lives in the backend.
//
// action: 'start' (no history) | 'increase' | 'repeat' | 'deload'.
// weightGrams is null only for 'start'; setCount is how many working sets
// were done at the working weight last time.
public record ProgressionSuggestion(
        String action,
        Integer weightGrams,
        int targetReps,
        Integer setCount,
        ExerciseSession basedOn
) {}
