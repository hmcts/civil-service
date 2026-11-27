CREATE UNIQUE INDEX uq_draft_store_claimant_response_user_case
  ON dbs.draft_store(user_id, case_id)
  WHERE draft_type = 'DRAFT_CLAIMANT_RESPONSE' AND case_id IS NOT NULL;
