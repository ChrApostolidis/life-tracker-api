-- Money log: expenses and income share one table; type carries the direction
-- (amount_cents is always positive). occurred_on is a local calendar day
-- ('YYYY-MM-DD') rather than an instant — money is logged per day, and a plain
-- date can't shift across UTC midnight. Lexical order = chronological order.
CREATE TABLE money_entries (
    id           varchar(255) not null,
    type         varchar(255) not null,
    amount_cents integer not null,
    label        varchar(255) not null,
    category     varchar(255),
    occurred_on  varchar(255) not null,
    deleted_at   timestamp,
    created_at   timestamp not null,
    updated_at   timestamp,
    primary key (id)
);

CREATE INDEX idx_money_entries_occurred_on ON money_entries(occurred_on);
