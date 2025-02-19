CREATE SEQUENCE IF NOT EXISTS dbs.notification_exception_record_seq
  START WITH 1
  INCREMENT BY 1
  NO MINVALUE
  NO MAXVALUE
  CACHE 1;

CREATE TABLE IF NOT EXISTS dbs.notification_exception_record (
  id BIGINT PRIMARY KEY DEFAULT nextval('dbs.notification_exception_record_seq'),
  reference VARCHAR(256),
  event_id VARCHAR(256),
  party_type VARCHAR(256),
  retry_count INT,
  created_at TIMESTAMP default CURRENT_TIMESTAMP,
  updated_on TIMESTAMP
);


