/**
 * Add scenario for claimant
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.GeneralApps.MoreInfoRequired.Applicant',
        '{"Notice.AAA6.GeneralApps.ApplicationSubmitted.Applicant", "Notice.AAA6.GeneralApps.RespondentResponseSubmitted.Applicant", "Notice.AAA6.GeneralApps.HwF.FullRemission.Applicant", "Notice.AAA6.GeneralApps.HwF.FeePaid.Applicant"}',
        '{"Notice.AAA6.GeneralApps.MoreInfoRequired.Applicant": ["judgeRequestMoreInfoByDateEn", "judgeRequestMoreInfoByDateCy"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.GeneralApps.MoreInfoRequired.Applicant',
        'You must provide more information',
        'Rhaid i chi ddarparu mwy o wybodaeth',
        '<p class="govuk-body">The court has responded to the application. You must upload a document<a href="{GA_VIEW_APPLICATION_URL}" class="govuk-link"> providing more information</a> to the court by 4pm on ${judgeRequestMoreInfoByDateEn}.</p>',
        '<p class="govuk-body">Mae’r llys wedi ymateb i’r cais. Rhaid i chi uwchlwytho dogfen sy’n <a href="{GA_VIEW_APPLICATION_URL}" class="govuk-link"> rhoi mwy o wybodaeth</a> i’r llys erbyn 4pm ar ${judgeRequestMoreInfoByDateCy}.</p>',
        'APPLICANT');
