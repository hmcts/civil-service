/*
Update QM Notification from View the message to View your messages
*/
UPDATE dbs.dashboard_notifications_templates SET description_en = replace(description_en, 'View the message', 'View your messages') WHERE description_en like '%View the message</a></p>' ;
UPDATE dbs.dashboard_notifications_templates SET description_cy = replace(description_cy, 'Gweld y neges', 'Gweld eich negeseuon') WHERE description_cy like '%Gweld y neges</a></p>';

/*
Update QM Notification from View the message from the court to View your messages
 */
UPDATE dbs.dashboard_notifications_templates SET description_en = replace(description_en, 'View the message from the court', 'View your messages') WHERE description_en like '%View the message from the court</a></p>' ;
UPDATE dbs.dashboard_notifications_templates SET description_cy = replace(description_cy, 'Gweld y neges gan y llys', 'Gweld eich negeseuon') WHERE description_cy like '%Gweld y neges gan y llys</a></p>' ;
