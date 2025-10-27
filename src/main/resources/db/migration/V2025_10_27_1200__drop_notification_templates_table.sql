ALTER TABLE dbs.dashboard_notifications
    ADD COLUMN IF NOT EXISTS time_to_live VARCHAR(256);

UPDATE dbs.dashboard_notifications dn
SET time_to_live = tmpl.time_to_live
FROM dbs.dashboard_notifications_templates tmpl
WHERE dn.dashboard_notifications_templates_id = tmpl.id
  AND dn.time_to_live IS NULL;

DO $$
DECLARE
    constr RECORD;
BEGIN
    FOR constr IN
        SELECT conname
        FROM pg_constraint
        WHERE conrelid = 'dbs.dashboard_notifications'::regclass
          AND confrelid = 'dbs.dashboard_notifications_templates'::regclass
    LOOP
        EXECUTE format('ALTER TABLE dbs.dashboard_notifications DROP CONSTRAINT %I', constr.conname);
    END LOOP;
END $$;

ALTER TABLE dbs.dashboard_notifications
    DROP COLUMN IF EXISTS dashboard_notifications_templates_id;

DROP TABLE IF EXISTS dbs.dashboard_notifications_templates CASCADE;

DROP SEQUENCE IF EXISTS dbs.dashboard_notifications_templates_id_seq;
