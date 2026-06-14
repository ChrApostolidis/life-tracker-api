CREATE TABLE tasks (
                       id              TEXT PRIMARY KEY,
                       title           TEXT NOT NULL,
                       scheduled_at    DATETIME,
                       duration_min    INTEGER,
                       recurrence      TEXT,
                       recurrence_day  INTEGER,
                       recurrence_until DATETIME,
                       completed_at    DATETIME,
                       deleted_at      DATETIME,
                       source          TEXT NOT NULL DEFAULT 'text',
                       raw_transcript  TEXT,
                       created_at      DATETIME NOT NULL,
                       updated_at      DATETIME NOT NULL
);

CREATE TABLE notes (
                       id          TEXT PRIMARY KEY,
                       task_id     TEXT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
                       body        TEXT NOT NULL,
                       source      TEXT NOT NULL DEFAULT 'text',
                       created_at  DATETIME NOT NULL,
                       updated_at  DATETIME NOT NULL
);

CREATE TABLE task_overrides (
                                id              TEXT PRIMARY KEY,
                                template_id     TEXT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
                                occurrence_date DATE NOT NULL,
                                completed_at    DATETIME,
                                title           TEXT,
                                scheduled_at    DATETIME,
                                cancelled       INTEGER NOT NULL DEFAULT 0,
                                UNIQUE(template_id, occurrence_date)
);

CREATE INDEX idx_tasks_scheduled_at ON tasks(scheduled_at);
CREATE INDEX idx_task_overrides_template_occurrence ON task_overrides(template_id, occurrence_date);