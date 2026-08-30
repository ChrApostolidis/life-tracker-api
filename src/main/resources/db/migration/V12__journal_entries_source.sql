-- Match the convention tasks and notes already follow: record whether the text
-- was typed or dictated, and keep the original speech-to-text even after edits.
-- Only the transcript is ever stored; no audio is uploaded or kept anywhere.
ALTER TABLE journal_entries ADD COLUMN source varchar(255) not null default 'text';
ALTER TABLE journal_entries ADD COLUMN raw_transcript text;
