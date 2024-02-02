CREATE SEQUENCE dashboard_notifications_id_seq START 1 NO MAXVALUE NO MINVALUE INCREMENT BY 1 CACHE 1;

CREATE TABLE dashboard_notifications (
 id UUID DEFAULT uuid_generate_v4() PRIMARY KEY DEFAULT nextval('dashboard_notifications_id_seq'),
 dashboard_notifications_Templates_id INTEGER REFERENCES dashboard_notifications_Templates(id),
 reference VARCHAR(256),
 enHTML VARCHAR(256),
 cyHTML VARCHAR(256),
 message_param VARCHAR(256),
 createdBy VARCHAR(256),
 createdAt DATE,
 updatedBy VARCHAR(256),
 updatedOn DATE,
);
