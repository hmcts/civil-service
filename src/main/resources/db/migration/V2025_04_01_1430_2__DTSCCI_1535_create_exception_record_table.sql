CREATE TABLE IF NOT EXISTS dbs.exception_record (
  idempotency_key VARCHAR PRIMARY KEY,
  reference VARCHAR(256),
  task_id VARCHAR(256),
  remaining_retries INT,
  successful_actions TEXT ARRAY,
  created_at TIMESTAMP default CURRENT_TIMESTAMP,
  updated_on TIMESTAMP
);
