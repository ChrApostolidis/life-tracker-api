-- One free-form journal entry per calendar day. entry_date is a local
-- 'YYYY-MM-DD' (same convention as money_entries.occurred_on) and is UNIQUE —
-- a day has at most one entry. Kept in its own table rather than overloading
-- notes: a journal entry is addressed by date, notes are addressed by id.
CREATE TABLE day_notes (
    id         varchar(255) not null,
    entry_date varchar(255) not null,
    body       text not null,
    rating     integer,
    deleted_at timestamp,
    created_at timestamp not null,
    updated_at timestamp,
    primary key (id)
);

CREATE UNIQUE INDEX idx_day_notes_date ON day_notes(entry_date);
