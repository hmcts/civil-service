/**
 * Adding draft store tables
 */

CREATE TABLE draft_type
(
  ID INT NOT NULL,
  DRAFT_TYPE VARCHAR(200) NOT NULL,
  PRIMARY KEY (ID)
);

CREATE TABLE draft_store
(
  ID VARCHAR(120) NOT NULL,
  USER_ID  VARCHAR(200) NOT NULL,
  CASE_ID VARCHAR(200),
  DRAFT_TYPE_ID INT NOT NULL,
  PAYLOAD JSONB NOT NULL,
  CREATED_AT TIMESTAMP default CURRENT_TIMESTAMP,
  UPDATED_AT TIMESTAMP default CURRENT_TIMESTAMP,
  EXPIRES_AT TIMESTAMP,
  PRIMARY KEY (ID),
  FOREIGN KEY (DRAFT_TYPE_ID) REFERENCES dbs.draft_type(id)
);

-- Index on foreign key (Crucial for joins and deletions on draft_type)
CREATE INDEX idx_draft_store_draft_type_id ON draft_store(DRAFT_TYPE_ID);

-- Index for user lookups (Assuming you'll query "get drafts for this user")
CREATE INDEX idx_draft_store_user_id ON draft_store(USER_ID);

-- Index for cleanup cron jobs (Assuming you'll delete expired drafts regularly)
CREATE INDEX idx_draft_store_expires_at ON draft_store(EXPIRES_AT);
