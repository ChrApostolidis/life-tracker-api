-- Notes: standalone (task_id IS NULL) or attached to a task. task_id is nullable
-- from day one so the standalone notes library and task-attached notes share one table.
CREATE TABLE notes (
    id             varchar(255) not null,
    task_id        varchar(255) references tasks(id),
    body           varchar(255) not null,
    source         varchar(255) not null default 'text',
    raw_transcript varchar(255),
    deleted_at     timestamp,
    created_at     timestamp not null,
    updated_at     timestamp,
    primary key (id)
);

CREATE INDEX idx_notes_task_id ON notes(task_id);
