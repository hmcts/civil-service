/**
 * Update scenario
 */
UPDATE dbs.scenario
SET notifications_to_create = '{"Notice.AAA6.DefResponse.FullOrPartAdmit.PayImmediately.Defendant" : ["descriptionEn", "descriptionCy"]}'
WHERE name ='Scenario.AAA6.DefResponse.FullOrPartAdmit.PayImmediately.Defendant';

/**
 * Update notification template
 */
UPDATE dbs.dashboard_notifications_templates
SET description_En = '${descriptionEn}',
    description_Cy = '${descriptionCy}'
WHERE template_name = 'Notice.AAA6.DefResponse.FullOrPartAdmit.PayImmediately.Defendant' and notification_role = 'DEFENDANT';

