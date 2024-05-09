CREATE SEQUENCE IF NOT EXISTS dbs.notification_action_id_seq
  START WITH 1
  INCREMENT BY 1
  NO MINVALUE
  NO MAXVALUE
  CACHE 1;

CREATE TABLE IF NOT EXISTS dbs.notification_action (
  id BIGINT NOT NULL PRIMARY KEY,
  dashboard_notifications_id UUID,
  reference character varying(256),
  action_performed character varying(256),
  created_by character varying(256),
  created_at TIMESTAMP default CURRENT_TIMESTAMP
  );

CREATE TABLE IF NOT EXISTS dbs.dashboard_notifications (
 id UUID NOT NULL PRIMARY KEY,
 dashboard_notifications_templates_id INTEGER REFERENCES dbs.dashboard_notifications_templates(id),
 notification_action_id INTEGER REFERENCES dbs.notification_action(id),
 reference VARCHAR(256),
 notification_name VARCHAR(256),
 citizen_role VARCHAR(256),
 title_en VARCHAR(256),
 title_cy VARCHAR(256),
 description_en VARCHAR(2048),
 description_cy VARCHAR(2048),
 message_params jsonb,
 created_by VARCHAR(256),
 created_at TIMESTAMP default CURRENT_TIMESTAMP,
 updated_by VARCHAR(256),
 updated_on TIMESTAMP default CURRENT_TIMESTAMP
);

ALTER TABLE dbs.notification_action
ADD CONSTRAINT fk_dashboard_notifications_id FOREIGN KEY (dashboard_notifications_id)
REFERENCES dbs.dashboard_notifications(id);
