/**
 * Add scenario for claimant
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.GeneralApps.HearingScheduled.Respondent',
        '{"Notice.AAA6.GeneralApps.OrderMade.Respondent"}',
        '{"Notice.AAA6.GeneralApps.HearingScheduled.Respondent": ["hearingNoticeApplicationDateEn", "hearingNoticeApplicationDateCy"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.GeneralApps.HearingScheduled.Respondent',
        'A hearing on the application has been scheduled',
        'Mae gwrandawiad ar y cais wedi’i drefnu',
        '<p class="govuk-body">Your hearing has been scheduled for ${hearingNoticeApplicationDateEn} <a href="{GA_RESPONSE_VIEW_APPLICATION_URL}" class="govuk-link"> View the hearing notice.</a></p>',
        '<p class="govuk-body">Mae eich gwrandawiad wedi’i drefnu ar gyfer ${hearingNoticeApplicationDateCy} <a href="{GA_RESPONSE_VIEW_APPLICATION_URL}" class="govuk-link"> Gweld yr hysbysiad o wrandawiad.</a></p>',
        'RESPONDENT');
