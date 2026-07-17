-- Per-day completion state for recurring task occurrences. A recurring task
-- (tasks.recurrence NOT NULL) is a template row, expanded into occurrences at
-- read time; completing one occurrence writes a row here instead of mutating
-- the template's completed_at, which stays NULL forever for a template.
CREATE TABLE task_completions (
    id              varchar(255) not null,
    task_id         varchar(255) not null,
    occurrence_date varchar(255) not null,
    completed_at    timestamp not null,
    created_at      timestamp not null,
    primary key (id),
    unique (task_id, occurrence_date)
);

CREATE INDEX idx_task_completions_task_id ON task_completions(task_id);
