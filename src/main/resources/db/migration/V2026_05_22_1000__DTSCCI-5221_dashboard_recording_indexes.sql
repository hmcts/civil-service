CREATE INDEX IF NOT EXISTS idx_dashboard_notifications_ref_role_name
    ON dbs.dashboard_notifications(reference, citizen_role, notification_name);

CREATE INDEX IF NOT EXISTS idx_notification_action_dashboard_id_action
    ON dbs.notification_action(dashboard_notifications_id, action_performed);
