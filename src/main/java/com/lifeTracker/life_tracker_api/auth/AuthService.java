package com.lifeTracker.life_tracker_api.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Single-user auth: one BCrypt password from the environment, and sessions
 * that each device keeps until it logs out or goes unused for the lifetime.
 */
@Service
public class AuthService {

    private static final Pattern BCRYPT = Pattern.compile("^\\$2[aby]\\$\\d{2}\\$.{53}$");
    // Sliding expiry is pushed back at most this often, so an active session
    // does not cost a database write on every single request.
    private static final Duration RENEW_AFTER = Duration.ofHours(1);

    private final SessionRepository sessionRepository;
    private final Clock clock;
    private final String passwordHash;
    private final byte[] serviceTokenHash;
    private final Duration lifetime;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final SecureRandom random = new SecureRandom();

    public record Authenticated(Session session, boolean renewed) {}

    public AuthService(
            SessionRepository sessionRepository,
            Clock clock,
            @Value("${app.auth.password-hash:}") String passwordHash,
            @Value("${app.auth.service-token:}") String serviceToken,
            @Value("${app.auth.session-days:30}") int sessionDays
    ) {
        this.sessionRepository = sessionRepository;
        this.clock = clock;
        this.passwordHash = passwordHash.trim();
        this.serviceTokenHash = serviceToken.isBlank() ? null : sha256(serviceToken.trim());
        this.lifetime = Duration.ofDays(sessionDays);
    }

    public boolean hasUsablePasswordHash() {
        return BCRYPT.matcher(passwordHash).matches();
    }

    public boolean passwordMatches(String password) {
        return hasUsablePasswordHash() && password != null && encoder.matches(password, passwordHash);
    }

    public Duration lifetime() {
        return lifetime;
    }

    /** Starts a session and returns its token. Only the token's hash is stored. */
    @Transactional
    public String startSession(String client) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        Instant now = clock.instant();
        Session session = new Session();
        session.setId(UUID.randomUUID().toString());
        session.setTokenHash(HexFormat.of().formatHex(sha256(token)));
        session.setClient(client);
        session.setCreatedAt(now);
        session.setLastUsedAt(now);
        session.setExpiresAt(now.plus(lifetime));
        sessionRepository.save(session);
        return token;
    }

    /** The live session for this token, renewing its expiry when due. */
    @Transactional
    public Optional<Authenticated> authenticate(String token) {
        Instant now = clock.instant();
        return findSession(token)
                .filter(session -> session.isLiveAt(now))
                .map(session -> {
                    boolean due = Duration.between(session.getLastUsedAt(), now).compareTo(RENEW_AFTER) >= 0;
                    if (due) {
                        session.setLastUsedAt(now);
                        session.setExpiresAt(now.plus(lifetime));
                        sessionRepository.save(session);
                    }
                    return new Authenticated(session, due);
                });
    }

    /** The long-lived token the n8n bot sends. Off when none is configured. */
    public boolean isServiceToken(String token) {
        // MessageDigest.isEqual is constant-time, so timing reveals nothing.
        return serviceTokenHash != null && MessageDigest.isEqual(serviceTokenHash, sha256(token));
    }

    /** Ends a session. A token that is unknown or already ended is a no-op. */
    @Transactional
    public void revoke(String token) {
        findSession(token)
                .filter(session -> session.getRevokedAt() == null)
                .ifPresent(session -> {
                    session.setRevokedAt(clock.instant());
                    sessionRepository.save(session);
                });
    }

    private Optional<Session> findSession(String token) {
        return sessionRepository.findByTokenHash(HexFormat.of().formatHex(sha256(token)));
    }

    private static byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is missing from this JVM", e); // guaranteed by the Java spec
        }
    }
}
