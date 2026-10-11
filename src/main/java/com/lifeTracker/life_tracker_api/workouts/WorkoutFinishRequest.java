package com.lifeTracker.life_tracker_api.workouts;

import java.time.Instant;

// Optional body for /finish. An offline client sends when Finish was actually
// tapped; without it the server stamps now.
public record WorkoutFinishRequest(Instant finishedAt) {}
