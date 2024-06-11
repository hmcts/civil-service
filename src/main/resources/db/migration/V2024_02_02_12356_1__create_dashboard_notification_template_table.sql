CREATE SCHEMA IF NOT EXISTS dbs;

CREATE SEQUENCE IF NOT EXISTS dbs.dashboard_notifications_templates_id_seq START 1 NO MAXVALUE NO MINVALUE INCREMENT BY 1 CACHE 1;

CREATE TABLE IF NOT EXISTS dbs.dashboard_notifications_templates (
 id BIGINT PRIMARY KEY DEFAULT nextval('dbs.dashboard_notifications_templates_id_seq'),
 template_name VARCHAR(256),
 title_en VARCHAR(256),
 title_cy VARCHAR(256),
 description_en VARCHAR(2048),
 description_cy VARCHAR(2048),
 notification_role VARCHAR(256),
 time_to_live VARCHAR(256),
 created_at TIMESTAMP default CURRENT_TIMESTAMP
);
