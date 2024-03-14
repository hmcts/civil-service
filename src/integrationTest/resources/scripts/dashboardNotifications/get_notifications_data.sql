
INSERT INTO dbs.dashboard_notifications_templates
(id, template_name, title_en, title_cy, description_en, description_cy, notification_role, time_to_live, created_at)
VALUES(12255, 'template_name', 'title_en', 'title_cy', 'description_en', 'description_cy', 'notification_role', 'Click', now());


INSERT INTO dbs.dashboard_notifications
(id, dashboard_notifications_Templates_id, reference, notification_name, citizen_role, title_en, title_cy, description_en, description_cy, message_params, created_by, created_at, updated_by, updated_on)
VALUES('8c2712da-47ce-4050-bbee-650134a7b9e5', 12255, '127', 'notification_name', 'defendant', 'title_en', 'title_cy', 'description_en', 'description_cy', null, 'admin', '2024-02-10 10:00:00', 'admin', '2024-02-10 10:00:00');
