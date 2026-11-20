ALTER TABLE dbs.draft_store ADD COLUMN application_id VARCHAR(200);

CREATE UNIQUE INDEX uq_draft_store_ga_user_case
    ON dbs.draft_store (user_id, case_id)
    WHERE draft_type = 'DRAFT_GENERAL_APPLICATION' AND case_id IS NOT NULL;

CREATE UNIQUE INDEX uq_draft_store_ga_response_user_application
    ON dbs.draft_store (user_id, application_id)
    WHERE draft_type = 'DRAFT_GENERAL_APPLICATION_RESPONSE' AND application_id IS NOT NULL;
