CREATE TABLE dbs.draft_type (
       Id int NOT NULL PRIMARY KEY,
       draft_type VARCHAR(200) NOT NULL
);

CREATE TABLE dbs.draft_store (
       draft_id UUID NOT NULL PRIMARY KEY,
       user_id VARCHAR(200) NOT NULL,
       case_id VARCHAR(200),
       draft_type_id int NOT NULL,
       draft_claim VARCHAR(200) NOT NULL,
       payload JSONB NOT NULL,
       draft_claim_created_at TIMESTAMP default CURRENT_TIMESTAMP NOT NULL,
       created_at TIMESTAMP default CURRENT_TIMESTAMP NOT NULL,
       updated_at TIMESTAMP default CURRENT_TIMESTAMP NOT NULL,
       expires_at TIMESTAMP GENERATED ALWAYS AS (draft_claim_created_at + INTERVAL ‘180 days’) STORED NOT NULL,
       FOREIGN KEY (draft_type_id) REFERENCES dbs.draft_type(id)
);

CREATE INDEX idx_draft_store_user_id_draft_type_id ON dbs.draft_store (user_id, draft_type_id);
CREATE INDEX idx_draft_store_expires_at ON dbs.draft_store (expires_at);
