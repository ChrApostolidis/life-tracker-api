-- Movies and series in one table; media_type discriminates. tmdb_id is the
-- upstream id, kept so we can re-fetch episode lists without a second lookup.
CREATE TABLE watch_items (
    id             varchar(255) not null,
    tmdb_id        integer not null,
    media_type     varchar(255) not null,   -- 'movie' | 'series'
    title          varchar(255) not null,
    year           varchar(255),            -- 'YYYY' from release_date/first_air_date, may be absent upstream
    poster_url     varchar(255),
    status         varchar(255) not null,   -- 'watchlist' | 'watching' | 'watched' | 'dropped'
    rating         integer,                 -- 1–5, only meaningful once watched
    started_on     varchar(255),            -- local 'YYYY-MM-DD', auto-stamped on -> watching
    finished_on    varchar(255),            -- local 'YYYY-MM-DD', auto-stamped on -> watched
    total_seasons  integer,                 -- series only, snapshot at add time
    total_episodes integer,                 -- series only, snapshot at add time
    notes          text,
    deleted_at     timestamp,
    created_at     timestamp not null,
    updated_at     timestamp,
    primary key (id)
);

-- The same title can't be added twice. Includes soft-deleted rows, so re-adding
-- something you deleted revives it rather than colliding — see WatchItemService.
CREATE UNIQUE INDEX idx_watch_items_tmdb ON watch_items(tmdb_id, media_type);

-- One row per watched episode. Un-watching DELETES the row — same reasoning as
-- habit_checks (a toggle, not a document), and the unique index below would
-- reject re-watching an episode if a tombstoned row lingered.
CREATE TABLE episode_watches (
    id             varchar(255) not null,
    watch_item_id  varchar(255) not null references watch_items(id),
    season_number  integer not null,
    episode_number integer not null,
    created_at     timestamp not null,
    primary key (id)
);

CREATE UNIQUE INDEX idx_episode_watches_unique ON episode_watches(watch_item_id, season_number, episode_number);
CREATE INDEX idx_episode_watches_item ON episode_watches(watch_item_id);
