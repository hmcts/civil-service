/**
 * Dashboard notification 1
 */
UPDATE dbs.dashboard_notifications_templates SET description_En = '<p class="govuk-body">Your hearing has been scheduled for ${hearingNoticeApplicationDateEn} <a href="{GA_VIEW_APPLICATION_URL}" class="govuk-link"> View the hearing notice</a>.</p>' WHERE template_name = 'Notice.AAA6.GeneralApps.HearingScheduled.Applicant';

UPDATE dbs.dashboard_notifications_templates SET description_Cy = '<p class="govuk-body">Mae eich gwrandawiad wedi’i drefnu ar gyfer ${hearingNoticeApplicationDateCy} <a href="{GA_VIEW_APPLICATION_URL}" class="govuk-link"> Gweld yr hysbysiad o wrandawiad</a>.</p>' WHERE template_name = 'Notice.AAA6.GeneralApps.HearingScheduled.Applicant';

/**
 * Dashboard notification 2
 */
UPDATE dbs.dashboard_notifications_templates SET description_En = '<p class="govuk-body">Your hearing has been scheduled for ${hearingNoticeApplicationDateEn} <a href="{GA_RESPONSE_VIEW_APPLICATION_URL}" class="govuk-link"> View the hearing notice</a>.</p>' WHERE template_name = 'Notice.AAA6.GeneralApps.HearingScheduled.Respondent';

UPDATE dbs.dashboard_notifications_templates SET description_Cy = '<p class="govuk-body">Mae eich gwrandawiad wedi’i drefnu ar gyfer ${hearingNoticeApplicationDateCy} <a href="{GA_RESPONSE_VIEW_APPLICATION_URL}" class="govuk-link"> Gweld yr hysbysiad o wrandawiad</a>.</p>' WHERE template_name = 'Notice.AAA6.GeneralApps.HearingScheduled.Respondent';
