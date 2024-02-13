CREATE TABLE IF NOT EXISTS dbs.dashboard_notifications (
 id UUID NOT NULL PRIMARY KEY,
 dashboard_notifications_Templates_id INTEGER REFERENCES dbs.dashboard_notifications_Templates(id),
 reference VARCHAR(256),
 notification_name VARCHAR(256),
 citizen_role VARCHAR(256),
 title_en VARCHAR(256),
 title_cy VARCHAR(256),
 description_en VARCHAR(256),
 description_cy VARCHAR(256),
 message_param VARCHAR(256),
 created_by VARCHAR(256),
 created_at timestamp without time zone DEFAULT now() NOT NULL,
 updated_by VARCHAR(256),
 updated_on timestamp without time zone DEFAULT now() NOT NULL
);
