-- DTSCCI-6780: drop the unused reverse pointer, its FK, and its index together. No CASCADE.
-- Flyway 11 rejects mixing DROP INDEX CONCURRENTLY with these ALTER statements.
-- Keeping them in one transaction avoids a window where the reverse FK remains without its index.
SET lock_timeout = '10s';

DROP INDEX IF EXISTS dbs.idx_dashboard_notifications_notification_action_id;

ALTER TABLE dbs.dashboard_notifications
    DROP CONSTRAINT IF EXISTS dashboard_notifications_notification_action_id_fkey;

ALTER TABLE dbs.dashboard_notifications
    DROP COLUMN IF EXISTS notification_action_id;
