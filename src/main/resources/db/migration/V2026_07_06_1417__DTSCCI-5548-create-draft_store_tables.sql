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

CREATE INDEX idx_draft_store_draft_type_id ON dbs.draft_store(DRAFT_TYPE_ID);
CREATE INDEX idx_draft_store_user_id ON dbs.draft_store(USER_ID);
CREATE INDEX idx_draft_store_expires_at ON dbs.draft_store(EXPIRES_AT);
