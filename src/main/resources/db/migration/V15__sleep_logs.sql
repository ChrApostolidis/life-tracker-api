-- One night of sleep per row. sleep_date is the local 'YYYY-MM-DD' of the
-- morning you woke (Sun 23:40 -> Mon 07:00 is Monday's) and is UNIQUE, so it
-- shares day_notes' soft-delete + revive shape. bed_at/woke_at are UTC instants,
-- which keeps durations right across midnight and DST changes.
CREATE TABLE sleep_logs (
    id         varchar(255) not null,
    sleep_date varchar(255) not null,
    bed_at     timestamp not null,
    woke_at    timestamp not null,
    deleted_at timestamp,
    created_at timestamp not null,
    updated_at timestamp,
    primary key (id)
);

CREATE UNIQUE INDEX idx_sleep_logs_date ON sleep_logs(sleep_date);
