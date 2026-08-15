-- Genre names snapshotted from TMDB at add time, comma-separated ('Comedy, Drama').
-- A snapshot for the same reason total_episodes is one: it's display metadata, not
-- a relationship worth a join table for a single-user app.
ALTER TABLE watch_items ADD COLUMN genres varchar(255);

-- The pipeline drops 'dropped', and 'watching' now only applies to series --
-- a movie is either on the list or seen, there's no meaningful middle.
-- Both collapse back to 'watchlist': neither one was finished.
UPDATE watch_items SET status = 'watchlist' WHERE status = 'dropped';
UPDATE watch_items SET status = 'watchlist' WHERE status = 'watching' AND media_type = 'movie';
