-- Free-form labels on a project ("work", "weekend", "client"), so the pools can
-- be filtered. Comma-separated like tech_stack and journal_entries.tags, capped
-- at 5 by the frontend.
ALTER TABLE projects ADD COLUMN tags varchar(500);
