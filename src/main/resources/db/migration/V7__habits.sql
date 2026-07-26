-- Habits: binary daily checks, no scheduling. A habit is never hard-deleted —
-- archived_at hides it from the checklists while keeping its check history.
CREATE TABLE habits (
    id          varchar(255) not null,
    name        varchar(255) not null,
    archived_at timestamp,
    created_at  timestamp not null,
    updated_at  timestamp,
    primary key (id)
);

-- One row per (habit, day) = that habit was done that day. checked_on is a
-- local calendar day ('YYYY-MM-DD'), same convention as money_entries.occurred_on.
-- Unchecking DELETES the row (see note in HabitService) — there is no deleted_at
-- here on purpose, so the unique index below stays correct across re-checks.
CREATE TABLE habit_checks (
    id         varchar(255) not null,
    habit_id   varchar(255) not null references habits(id),
    checked_on varchar(255) not null,
    created_at timestamp not null,
    primary key (id)
);

CREATE UNIQUE INDEX idx_habit_checks_habit_day ON habit_checks(habit_id, checked_on);
CREATE INDEX idx_habit_checks_day ON habit_checks(checked_on);
