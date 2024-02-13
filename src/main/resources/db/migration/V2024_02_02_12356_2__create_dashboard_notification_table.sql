CREATE TABLE IF NOT EXISTS dbs.dashboard_notifications (
 id UUID NOT NULL PRIMARY KEY,
 dashboard_notifications_templates_id INTEGER REFERENCES dbs.dashboard_notifications_templates(id),
 reference VARCHAR(256),
 notification_name VARCHAR(256),
 citizen_Role VARCHAR(256),
 title_En VARCHAR(256),
 title_Cy VARCHAR(256),
 description_En VARCHAR(256),
 description_Cy VARCHAR(256),
 message_param VARCHAR(256),
 created_By VARCHAR(256),
 created_At timestamp without time zone DEFAULT now() NOT NULL,
 updated_By VARCHAR(256),
 updated_On timestamp without time zone DEFAULT now() NOT NULL
);
