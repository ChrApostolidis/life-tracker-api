package com.lifeTracker.life_tracker_api.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class LoginRateLimiterTest {

    private MutableClock clock;
    private LoginRateLimiter limiter;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2026-10-03T09:00:00Z"));
        limiter = new LoginRateLimiter(clock);
    }

    private void fail(String caller, int times) {
        for (int i = 0; i < times; i++) limiter.recordFailure(caller);
    }

    @Test
    void fourWrongGuessesAreAllowed() {
        fail("1.1.1.1", LoginRateLimiter.MAX_FAILURES - 1);
        assertEquals(0, limiter.secondsLocked("1.1.1.1"));
    }

    @Test
    void theFifthLocksForFifteenMinutes() {
        fail("1.1.1.1", LoginRateLimiter.MAX_FAILURES);
        assertEquals(LoginRateLimiter.LOCKOUT.toSeconds(), limiter.secondsLocked("1.1.1.1"));
        clock.advance(LoginRateLimiter.LOCKOUT.minusSeconds(1));
        assertTrue(limiter.secondsLocked("1.1.1.1") > 0);
        clock.advance(Duration.ofSeconds(1));
        assertEquals(0, limiter.secondsLocked("1.1.1.1"));
    }

    @Test
    void oneCallerBeingLockedDoesNotLockAnother() {
        fail("1.1.1.1", LoginRateLimiter.MAX_FAILURES);
        assertEquals(0, limiter.secondsLocked("2.2.2.2"));
    }

    @Test
    void aSuccessClearsEarlierFailures() {
        fail("1.1.1.1", LoginRateLimiter.MAX_FAILURES - 1);
        limiter.recordSuccess("1.1.1.1");
        fail("1.1.1.1", LoginRateLimiter.MAX_FAILURES - 1);
        assertEquals(0, limiter.secondsLocked("1.1.1.1"));
    }
}
