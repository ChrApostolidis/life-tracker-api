package com.lifeTracker.life_tracker_api.workouts;

// Ids a client may mint itself. They end up in URL paths, so only a safe
// charset is accepted — a UUID fits.
final class ClientIds {

    static final String PATTERN = "[A-Za-z0-9-]{1,64}";

    private ClientIds() {}
}
