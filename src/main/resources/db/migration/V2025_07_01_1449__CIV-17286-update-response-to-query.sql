/**
 * Update claimant notification
 */
UPDATE dbs.dashboard_notifications_templates SET title_En = 'Important' WHERE template_name = 'Notice.AAA6.LiPQM.QueryResponded.Claimant';

UPDATE dbs.dashboard_notifications_templates SET description_En = '<p class="govuk-body">The court has responded to a message on your case.<br> <a href="{QM_VIEW_MESSAGES_URL_CLICK}" rel="noopener noreferrer" class="govuk-link">View the message from the court</a></p>' WHERE template_name = 'Notice.AAA6.LiPQM.QueryResponded.Claimant';

UPDATE dbs.dashboard_notifications_templates SET title_Cy = 'Pwysig' WHERE template_name = 'Notice.AAA6.LiPQM.QueryResponded.Claimant';

UPDATE dbs.dashboard_notifications_templates SET description_Cy = '<p class="govuk-body">Mae’r llys wedi ymateb i neges ar eich achos.<br> <a href="{QM_VIEW_MESSAGES_URL_CLICK}" rel="noopener noreferrer" class="govuk-link">Gallwch weld y neges gan y llys</a></p>' WHERE template_name = 'Notice.AAA6.LiPQM.QueryResponded.Claimant';

/**
 * Update defendant notification
 */
UPDATE dbs.dashboard_notifications_templates SET title_En = 'Important' WHERE template_name = 'Notice.AAA6.LiPQM.QueryResponded.Defendant';

UPDATE dbs.dashboard_notifications_templates SET description_En = '<p class="govuk-body">The court has responded to a message on your case.<br> <a href="{QM_VIEW_MESSAGES_URL_CLICK}" rel="noopener noreferrer" class="govuk-link">View the message from the court</a></p>' WHERE template_name = 'Notice.AAA6.LiPQM.QueryResponded.Defendant';

UPDATE dbs.dashboard_notifications_templates SET title_Cy = 'Pwysig' WHERE template_name = 'Notice.AAA6.LiPQM.QueryResponded.Defendant';

UPDATE dbs.dashboard_notifications_templates SET description_Cy = '<p class="govuk-body">Mae’r llys wedi ymateb i neges ar eich achos.<br> <a href="{QM_VIEW_MESSAGES_URL_CLICK}" rel="noopener noreferrer" class="govuk-link">Gallwch weld y neges gan y llys</a></p>' WHERE template_name = 'Notice.AAA6.LiPQM.QueryResponded.Defendant';
