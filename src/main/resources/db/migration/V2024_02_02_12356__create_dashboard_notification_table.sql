CREATE OR REPLACE TABLE dashboard_notifications (
 id UUID DEFAULT uuid_generate_v4() PRIMARY KEY DEFAULT,
 dashboard_notifications_Templates_id INTEGER REFERENCES dashboard_notifications_Templates(id),
 reference VARCHAR(256),
 notification_name VARCHAR(256),
 en_HTML VARCHAR(256),
 cy_HTML VARCHAR(256),
 message_param VARCHAR(256),
 created_By VARCHAR(256),
 created_At DATE,
 updated_By VARCHAR(256),
 updated_On DATE,
);
