-- Book library. status is a pipeline: wishlist (want to buy) -> owned
-- (bought, not started) -> reading -> finished. started_on / finished_on are
-- local calendar days ('YYYY-MM-DD'), same convention as money_entries.occurred_on.
CREATE TABLE books (
    id          varchar(255) not null,
    title       varchar(255) not null,
    author      varchar(255),
    status      varchar(255) not null,
    started_on  varchar(255),
    finished_on varchar(255),
    rating      integer,
    cover_url   varchar(255),
    notes       text,
    deleted_at  timestamp,
    created_at  timestamp not null,
    updated_at  timestamp,
    primary key (id)
);
