package com.lifeTracker.life_tracker_api.auth;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * One logged-in device. Holds the SHA-256 of its token, never the token, so
 * the database alone cannot be used to sign in.
 */
@Entity
@Table(name = "sessions")
public class Session {

    @Id
    private String id;

    @Column(nullable = false, unique = true)
    private String tokenHash;

    // 'web' | 'extension' | 'mobile' — web sessions travel as a cookie, the
    // rest as a Bearer token.
    @Column(nullable = false)
    private String client;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant lastUsedAt;

    @Column(nullable = false)
    private Instant expiresAt;

    private Instant revokedAt;

    public boolean isLiveAt(Instant now) {
        return revokedAt == null && now.isBefore(expiresAt);
    }

    // getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }

    public String getClient() { return client; }
    public void setClient(String client) { this.client = client; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getLastUsedAt() { return lastUsedAt; }
    public void setLastUsedAt(Instant lastUsedAt) { this.lastUsedAt = lastUsedAt; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public Instant getRevokedAt() { return revokedAt; }
    public void setRevokedAt(Instant revokedAt) { this.revokedAt = revokedAt; }
}
