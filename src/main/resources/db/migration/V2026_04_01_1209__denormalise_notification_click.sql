ALTER TABLE dbs.dashboard_notifications
  ADD COLUMN IF NOT EXISTS clicked_by VARCHAR(256),
  ADD COLUMN IF NOT EXISTS clicked_at TIMESTAMP;

-- Migrate data from dbs.notification_action to dbs.dashboard_notifications if it exists
-- Use most recent unique notification action to populate the new columns matching previous update logic.
DO $$
BEGIN
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_schema = 'dbs' AND table_name = 'notification_action') THEN
        UPDATE dbs.dashboard_notifications dn
        SET clicked_by = na.created_by,
            clicked_at = na.created_at
        FROM (
            SELECT DISTINCT ON (dashboard_notifications_id) dashboard_notifications_id, created_by, created_at
            FROM dbs.notification_action
            WHERE action_performed = 'Click'
            ORDER BY dashboard_notifications_id, created_at DESC
        ) na
        WHERE dn.id = na.dashboard_notifications_id;
    END IF;
END $$;

-- Drop the foreign key constraint on dbs.dashboard_notifications
DO $$
DECLARE
    constr RECORD;
BEGIN
    FOR constr IN
        SELECT conname
        FROM pg_constraint
        WHERE conrelid = 'dbs.dashboard_notifications'::regclass
          AND confrelid = 'dbs.notification_action'::regclass
    LOOP
        EXECUTE format('ALTER TABLE dbs.dashboard_notifications DROP CONSTRAINT %I', constr.conname);
    END LOOP;
END $$;

-- Drop the notification_action_id column from dbs.dashboard_notifications
ALTER TABLE dbs.dashboard_notifications
    DROP COLUMN IF EXISTS notification_action_id;

-- Drop the dbs.notification_action table and sequence
DROP TABLE IF EXISTS dbs.notification_action CASCADE;
DROP SEQUENCE IF EXISTS dbs.notification_action_id_seq;
