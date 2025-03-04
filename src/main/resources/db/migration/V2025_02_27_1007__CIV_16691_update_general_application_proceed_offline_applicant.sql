/**
 * Update scenario for applicant & respondent
 */
UPDATE dbs.dashboard_notifications_templates SET notification_role = replace(notification_role, 'APPLICANT') WHERE template_name  = 'Notice.AAA6.GeneralApps.ApplicationProceedsOffline.Applicant' ;
UPDATE dbs.dashboard_notifications_templates SET notification_role = replace(notification_role, 'RESPONDENT') WHERE template_name  = 'Notice.AAA6.GeneralApps.ApplicationProceedsOffline.Respondent';
