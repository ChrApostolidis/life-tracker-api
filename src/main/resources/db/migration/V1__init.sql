-- Baseline schema: matches the Hibernate-generated (ddl-auto=update) schema that
-- production/dev DBs already have, so baselined and fresh databases converge.
-- Existing DBs are baselined at version 1 (baseline-on-migrate) and skip this file.
CREATE TABLE tasks (
    id               varchar(255) not null,
    completed_at     timestamp,
    created_at       timestamp not null,
    deleted_at       timestamp,
    duration_min     integer,
    raw_transcript   varchar(255),
    recurrence       varchar(255),
    recurrence_day   integer,
    recurrence_until timestamp,
    scheduled_at     timestamp,
    source           varchar(255) not null,
    title            varchar(255) not null,
    updated_at       timestamp,
    primary key (id)
);
