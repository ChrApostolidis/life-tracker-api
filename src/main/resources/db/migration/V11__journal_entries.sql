-- Long-form journal entries, alongside (not replacing) day_notes.
--
-- The defining difference from day_notes is what is NOT here: there is no
-- unique index on entry_date. Many entries per day is the entire feature.
-- day_notes keeps its one-per-day rule, its upsert-by-date endpoint, and its
-- place in the RPG streak maths.
CREATE TABLE journal_entries (
    id          varchar(255) not null,
    title       varchar(255),            -- optional; the list falls back to the first line of body
    body        text not null,
    tags        varchar(255),            -- comma-separated names, capped at 5 by the frontend
    entry_date  varchar(255) not null,   -- local 'YYYY-MM-DD': the day this entry is *about*
    deleted_at  timestamp,
    created_at  timestamp not null,
    updated_at  timestamp,
    primary key (id)
);

-- The timeline reads by date descending; entry_date is 'YYYY-MM-DD' so string
-- ordering is chronological.
CREATE INDEX idx_journal_entries_date ON journal_entries(entry_date);
