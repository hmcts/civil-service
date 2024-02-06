CREATE TABLE IF NOT EXISTS dashboard.dashboard_notifications (
 id UUID NOT NULL PRIMARY KEY,
 dashboard_notifications_Templates_id INTEGER REFERENCES dashboard.dashboard_notifications_Templates(id),
 reference VARCHAR(256),
 notification_name VARCHAR(256),
 en_HTML VARCHAR(256),
 cy_HTML VARCHAR(256),
 message_param VARCHAR(256),
 created_By VARCHAR(256),
 created_At timestamp without time zone DEFAULT now() NOT NULL,
 updated_By VARCHAR(256),
 updated_On timestamp without time zone DEFAULT now() NOT NULL
);
