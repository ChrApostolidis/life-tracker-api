package com.lifeTracker.life_tracker_api.auth;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Slows password guessing: after MAX_FAILURES wrong passwords in a row from one
 * client, that client is locked out for LOCKOUT. In memory, so a restart
 * clears it, which is fine for a single-user app.
 */
@Component
public class LoginRateLimiter {

    static final int MAX_FAILURES = 5;
    static final Duration LOCKOUT = Duration.ofMinutes(15);

    private record Attempts(int failures, Instant lockedUntil) {}

    private final Clock clock;
    private final Map<String, Attempts> attempts = new ConcurrentHashMap<>();

    public LoginRateLimiter(Clock clock) {
        this.clock = clock;
    }

    /** Seconds until this client may try again; 0 means it may try now. */
    public long secondsLocked(String client) {
        Attempts a = attempts.get(client);
        if (a == null || a.lockedUntil() == null) return 0;
        long seconds = Duration.between(clock.instant(), a.lockedUntil()).toSeconds();
        if (seconds > 0) return seconds;
        attempts.remove(client, a); // the lockout has passed
        return 0;
    }

    public void recordFailure(String client) {
        attempts.compute(client, (key, a) -> {
            int failures = (a == null ? 0 : a.failures()) + 1;
            // Hitting the limit locks and starts the count again, so each
            // lockout is followed by another MAX_FAILURES tries at most.
            return failures >= MAX_FAILURES
                    ? new Attempts(0, clock.instant().plus(LOCKOUT))
                    : new Attempts(failures, null);
        });
    }

    public void recordSuccess(String client) {
        attempts.remove(client);
    }
}
