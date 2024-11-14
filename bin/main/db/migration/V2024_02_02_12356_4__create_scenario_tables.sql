CREATE SEQUENCE IF NOT EXISTS dbs.scenario_id_seq
  START WITH 1
  INCREMENT BY 1
  NO MINVALUE
  NO MAXVALUE
  CACHE 1;

CREATE TABLE IF NOT EXISTS dbs.scenario (
       id BIGINT PRIMARY KEY DEFAULT nextval('dbs.scenario_id_seq'),
       name         character varying(256),
       notifications_to_delete         VARCHAR(256)[],
       notifications_to_create         jsonb,
       created_at TIMESTAMP default CURRENT_TIMESTAMP
);
