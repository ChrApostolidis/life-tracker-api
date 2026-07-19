-- book_id, like task_id, is nullable — lets a book's "thoughts" stream reuse
-- the existing notes table instead of a second per-entity notes model.
ALTER TABLE notes ADD COLUMN book_id varchar(255) references books(id);

CREATE INDEX idx_notes_book_id ON notes(book_id);
