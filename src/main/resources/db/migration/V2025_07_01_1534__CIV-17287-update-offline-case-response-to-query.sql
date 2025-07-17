/**
 * Update claimant notification
 */
UPDATE dbs.dashboard_notifications_templates SET title_En = 'Your case is now offline, but the court has not answered a message sent on the case' WHERE template_name = 'Notice.AAA6.LiPQM.CaseOffline.OpenQueries.Claimant';

UPDATE dbs.dashboard_notifications_templates SET description_En = '<p class="govuk-body">The message may still be answered on your dashboard, but all other updates on your case will be by email or post.</p>' WHERE template_name = 'Notice.AAA6.LiPQM.CaseOffline.OpenQueries.Claimant';

UPDATE dbs.dashboard_notifications_templates SET title_Cy = 'Mae eich achos bellach all-lein, ond nid yw’r llys wedi ateb neges a anfonwyd ar yr achos' WHERE template_name = 'Notice.AAA6.LiPQM.CaseOffline.OpenQueries.Claimant';

UPDATE dbs.dashboard_notifications_templates SET description_Cy = '<p class="govuk-body">Mae’n bosibl y byddwch yn dal i gael ateb i’ch neges ar eich dangosfwrdd, ond bydd yr holl ddiweddariadau eraill ar eich achos trwy e-bost neu''r post.</p>' WHERE template_name = 'Notice.AAA6.LiPQM.CaseOffline.OpenQueries.Claimant';

/**
 * Update defendant notification
 */
UPDATE dbs.dashboard_notifications_templates SET title_En = 'Your case is now offline, but the court has not answered a message sent on the case' WHERE template_name = 'Notice.AAA6.LiPQM.CaseOffline.OpenQueries.Defendant';

UPDATE dbs.dashboard_notifications_templates SET description_En = '<p class="govuk-body">The message may still be answered on your dashboard, but all other updates on your case will be by email or post.</p>' WHERE template_name = 'Notice.AAA6.LiPQM.CaseOffline.OpenQueries.Defendant';

UPDATE dbs.dashboard_notifications_templates SET title_Cy = 'Mae eich achos bellach all-lein, ond nid yw’r llys wedi ateb neges a anfonwyd ar yr achos' WHERE template_name = 'Notice.AAA6.LiPQM.CaseOffline.OpenQueries.Defendant';

UPDATE dbs.dashboard_notifications_templates SET description_Cy = '<p class="govuk-body">Mae’n bosibl y byddwch yn dal i gael ateb i’ch neges ar eich dangosfwrdd, ond bydd yr holl ddiweddariadau eraill ar eich achos trwy e-bost neu''r post.</p>' WHERE template_name = 'Notice.AAA6.LiPQM.CaseOffline.OpenQueries.Defendant';
