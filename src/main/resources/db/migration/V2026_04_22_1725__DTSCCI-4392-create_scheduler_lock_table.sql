CREATE TABLE lock_details_provider
(
  name       VARCHAR(64),
  lock_until TIMESTAMP(3) NULL,
  locked_at  TIMESTAMP(3) NULL,
  locked_by  VARCHAR(255),
  PRIMARY KEY (name)
)
