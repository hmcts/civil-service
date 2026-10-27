ALTER TABLE dbs.draft_store
  ADD COLUMN submission_id VARCHAR(200);

CREATE UNIQUE INDEX uq_draft_store_submission_id
  ON dbs.draft_store(submission_id)
  WHERE submission_id IS NOT NULL;
