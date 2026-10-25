-- Projects: side projects from idea to shipped, a four-column board of items
-- per project, and a dated log of progress updates. Plan in the web repo's
-- PROJECTS_PLAN.md. Soft delete everywhere; items and updates of a deleted
-- project are left in place and are simply unreachable through it.

-- status is a pipeline: idea -> ongoing -> finished. started_on / finished_on
-- are local calendar days ('YYYY-MM-DD'), auto-stamped like books'.
CREATE TABLE projects (
    id          varchar(255) not null,
    name        varchar(120) not null,
    pitch       varchar(200),            -- one-liner shown on pool cards
    description text,
    status      varchar(255) not null,   -- 'idea' | 'ongoing' | 'finished'
    repo_url    varchar(500),            -- http(s) only, checked by the service
    live_url    varchar(500),
    tech_stack  varchar(500),            -- comma-separated names, capped at 8 by the frontend
    started_on  varchar(255),
    finished_on varchar(255),
    deleted_at  timestamp,
    created_at  timestamp not null,
    updated_at  timestamp,
    primary key (id)
);

-- One card on a project's board. Named stage, not column: column is reserved SQL.
-- position orders cards within a stage and is gapped by 1024, so a move can
-- usually take the midpoint of its neighbours without touching anything else.
CREATE TABLE project_items (
    id           varchar(255) not null,
    project_id   varchar(255) not null references projects(id),
    title        varchar(200) not null,
    description  text,
    stage        varchar(255) not null,                    -- 'todo' | 'doing' | 'done' | 'blocked'
    priority     varchar(255) not null default 'normal',   -- 'low' | 'normal' | 'urgent'
    due_on       varchar(255),                             -- local 'YYYY-MM-DD'
    position     integer not null,
    completed_at timestamp,                                -- set while the item is in 'done'
    deleted_at   timestamp,
    created_at   timestamp not null,
    updated_at   timestamp,
    primary key (id)
);

CREATE INDEX idx_project_items_project ON project_items(project_id);

-- Dated progress notes. Many per day; entry_date is the day the update is about.
CREATE TABLE project_updates (
    id             varchar(255) not null,
    project_id     varchar(255) not null references projects(id),
    body           text not null,
    entry_date     varchar(255) not null,                  -- local 'YYYY-MM-DD'
    source         varchar(255) not null default 'text',   -- 'text' | 'voice', create-only
    raw_transcript text,
    deleted_at     timestamp,
    created_at     timestamp not null,
    updated_at     timestamp,
    primary key (id)
);

CREATE INDEX idx_project_updates_project ON project_updates(project_id);
