package com.lifeTracker.life_tracker_api.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

// Built by hand around the real repository so the clock can be moved forward.
// Each test rolls back, like the other service tests.
@SpringBootTest
@Transactional
class AuthServiceTest {

    private static final String PASSWORD = "correct horse battery staple";
    // Cost 4 keeps the suite fast; production hashes use the default 10 or more.
    private static final String HASH = new BCryptPasswordEncoder(4).encode(PASSWORD);

    @Autowired
    private SessionRepository sessionRepository;

    private MutableClock clock;
    private AuthService auth;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2026-10-03T09:00:00Z"));
        auth = new AuthService(sessionRepository, clock, HASH, "service-secret", 30);
    }

    @Test
    void acceptsOnlyTheRightPassword() {
        assertTrue(auth.passwordMatches(PASSWORD));
        assertFalse(auth.passwordMatches("wrong"));
        assertFalse(auth.passwordMatches(null));
    }

    @Test
    void noOneCanLogInWhileNoUsableHashIsConfigured() {
        AuthService blank = new AuthService(sessionRepository, clock, "", "", 30);
        AuthService plaintext = new AuthService(sessionRepository, clock, PASSWORD, "", 30);
        assertFalse(blank.hasUsablePasswordHash());
        assertFalse(blank.passwordMatches(""));
        // A password pasted where its hash belongs must not become the password.
        assertFalse(plaintext.hasUsablePasswordHash());
        assertFalse(plaintext.passwordMatches(PASSWORD));
    }

    @Test
    void storesTheTokensHashNeverTheToken() {
        String token = auth.startSession("web");
        Session stored = sessionRepository.findAll().stream()
                .filter(s -> s.getCreatedAt().equals(clock.instant()))
                .findFirst().orElseThrow();
        assertNotEquals(token, stored.getTokenHash());
        assertFalse(stored.getTokenHash().contains(token));
        assertEquals(64, stored.getTokenHash().length()); // SHA-256 as hex
    }

    @Test
    void aFreshSessionAuthenticates() {
        String token = auth.startSession("extension");
        assertTrue(auth.authenticate(token).isPresent());
        assertEquals("extension", auth.authenticate(token).orElseThrow().session().getClient());
    }

    @Test
    void anUnknownTokenDoesNot() {
        assertTrue(auth.authenticate("not-a-real-token").isEmpty());
    }

    @Test
    void thirtyDaysWithoutUseEndsTheSession() {
        String token = auth.startSession("web");
        clock.advance(Duration.ofDays(30));
        assertTrue(auth.authenticate(token).isEmpty());
    }

    @Test
    void usePushesTheExpiryBack() {
        String token = auth.startSession("web");
        clock.advance(Duration.ofDays(20));
        assertTrue(auth.authenticate(token).orElseThrow().renewed());
        // Past the original 30 days, but only 25 days since it was last used.
        clock.advance(Duration.ofDays(25));
        assertTrue(auth.authenticate(token).isPresent());
    }

    @Test
    void renewsAtMostOnceAnHour() {
        String token = auth.startSession("web");
        clock.advance(Duration.ofMinutes(30));
        assertFalse(auth.authenticate(token).orElseThrow().renewed());
        clock.advance(Duration.ofMinutes(31));
        assertTrue(auth.authenticate(token).orElseThrow().renewed());
    }

    @Test
    void logoutEndsTheSessionAndRepeatingItIsHarmless() {
        String token = auth.startSession("web");
        auth.revoke(token);
        assertTrue(auth.authenticate(token).isEmpty());
        assertDoesNotThrow(() -> auth.revoke(token));
        assertDoesNotThrow(() -> auth.revoke("never-existed"));
    }

    @Test
    void serviceTokenMatchesOnlyItself() {
        assertTrue(auth.isServiceToken("service-secret"));
        assertFalse(auth.isServiceToken("service-secreT"));
        AuthService none = new AuthService(sessionRepository, clock, HASH, "  ", 30);
        assertFalse(none.isServiceToken(""));
    }

    @Test
    void refusesToStartEnforcingWithoutAUsablePassword() {
        AuthService blank = new AuthService(sessionRepository, clock, "", "", 30);
        SessionCookies cookies = new SessionCookies("", true, 30);
        assertThrows(IllegalStateException.class, () -> new AuthInterceptor(true, blank, cookies));
        assertDoesNotThrow(() -> new AuthInterceptor(false, blank, cookies));
    }
}
