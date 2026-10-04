-- Login sessions for the single user. One row per device that logged in.
--
-- token_hash is the SHA-256 of the session token, never the token itself, so a
-- copy of this database contains no session anyone could use. A session is
-- live only while revoked_at is null and expires_at is in the future; logout
-- revokes rather than deletes, the same soft-delete convention as every table.
CREATE TABLE sessions (
    id            varchar(255) not null,
    token_hash    varchar(255) not null,
    client        varchar(255) not null,   -- 'web' | 'extension' | 'mobile'
    created_at    timestamp not null,
    last_used_at  timestamp not null,
    expires_at    timestamp not null,      -- pushed back on use: 30 days of inactivity ends it
    revoked_at    timestamp,
    primary key (id)
);

CREATE UNIQUE INDEX idx_sessions_token_hash ON sessions(token_hash);
