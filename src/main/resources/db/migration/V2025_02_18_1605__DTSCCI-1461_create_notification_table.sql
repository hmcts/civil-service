CREATE SEQUENCE IF NOT EXISTS dbs.notification_exception_record_seq
  START WITH 1
  INCREMENT BY 1
  NO MINVALUE
  NO MAXVALUE
  CACHE 1;

CREATE TABLE IF NOT EXISTS dbs.notification_exception_record (
  reference VARCHAR(256),
  task_id VARCHAR(256),
  party_type VARCHAR(256),
  retry_count INT,
  successful_actions TEXT ARRAY,
  created_at TIMESTAMP default CURRENT_TIMESTAMP,
  updated_on TIMESTAMP,
  PRIMARY KEY (reference, task_id)
);


