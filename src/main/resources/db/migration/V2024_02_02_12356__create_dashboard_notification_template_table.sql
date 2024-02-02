CREATE SEQUENCE dashboard_notifications_Templates_id_seq START 1 NO MAXVALUE NO MINVALUE INCREMENT BY 1 CACHE 1;

CREATE TABLE dashboard_notifications_Templates (
 id UUID DEFAULT uuid_generate_v4() PRIMARY KEY DEFAULT nextval('dashboard_notifications_Templates_id_seq'),
 name VARCHAR(256),
 engHTML VARCHAR(256),
 cyHTML VARCHAR(256),
 role VARCHAR(256),
 time_to_live VARCHAR(256),
 createdAt DATE
);
