
INSERT INTO dbs.dashboard_notifications
(id, reference, notification_name, citizen_role, title_en, title_cy, description_en, description_cy, message_params, created_by,
 created_at, updated_by, updated_on, time_to_live)
VALUES('8c2712da-47ce-4050-bbee-650134a7b9e6', '128', 'notification_multiple_actions', 'defendant', 'title_en', 'title_cy', 'description_en',
       'description_cy', null, 'admin', '2026-02-05 10:00:00.110362', 'admin', '2024-02-10 10:00:00', 'Click');

INSERT INTO dbs.notification_action
(id, reference, action_performed, created_by, created_at, dashboard_notifications_id)
VALUES(1001, '128', 'Click', 'User 1', '2026-02-06 13:08:04.110362', '8c2712da-47ce-4050-bbee-650134a7b9e6');

INSERT INTO dbs.notification_action
(id, reference, action_performed, created_by, created_at, dashboard_notifications_id)
VALUES(1002, '128', 'Click', 'User 1', '2026-02-06 13:08:04.163939', '8c2712da-47ce-4050-bbee-650134a7b9e6');
