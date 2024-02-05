CREATE SEQUENCE dashboard_notifications_Templates_id_seq START 1 NO MAXVALUE NO MINVALUE INCREMENT BY 1 CACHE 1;

CREATE OR REPLACE  TABLE dashboard_notifications_Templates (
 id BIGINT PRIMARY KEY DEFAULT nextval('dashboard_notifications_Templates_id_seq'),
 template_name VARCHAR(256),
 en_HTML VARCHAR(256),
 cy_HTML VARCHAR(256),
 notification_role VARCHAR(256),
 time_to_live VARCHAR(256),
 created_At timestamp without time zone DEFAULT now() NOT NULL
);
