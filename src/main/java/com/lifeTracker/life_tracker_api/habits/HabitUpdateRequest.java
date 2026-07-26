package com.lifeTracker.life_tracker_api.habits;

// PATCH payload — null = leave unchanged (same convention as tasks/money/books).
// Renaming is the only editable field; archiving is its own endpoint.
public record HabitUpdateRequest(
        String name
) {}
