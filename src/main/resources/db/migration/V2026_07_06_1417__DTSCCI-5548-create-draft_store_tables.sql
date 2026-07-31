/**
 * Adding draft store tables
 */

CREATE TABLE dbs.draft_type
(
  id INT NOT NULL,
  draft_type VARCHAR(200) NOT NULL,
  PRIMARY KEY (id)
);

INSERT INTO dbs.draft_type (id, draft_type)
VALUES (1, 'DRAFT_CLAIM');

INSERT INTO dbs.draft_type (id, draft_type)
VALUES (2, 'UNDEFINED_DRAFT');

CREATE TABLE dbs.draft_store
(
  id UUID NOT NULL,
  user_id VARCHAR(200) NOT NULL,
  case_id VARCHAR(200),
  draft_type_id INT NOT NULL,
  payload JSONB NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  expires_at TIMESTAMP NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (draft_type_id) REFERENCES dbs.draft_type(id)
);

CREATE INDEX idx_draft_store_user_type ON dbs.draft_store(user_id, draft_type_id);
CREATE INDEX idx_draft_store_expires_at ON dbs.draft_store(expires_at);
CREATE UNIQUE INDEX uq_draft_store_user_draft_claim ON dbs.draft_store(user_id) WHERE draft_type_id = 1;
