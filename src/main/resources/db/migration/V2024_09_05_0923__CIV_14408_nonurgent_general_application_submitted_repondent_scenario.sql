/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.GeneralApps.NonUrgentApplicationMade.Respondent', '{}', '{"Notice.AAA6.GeneralApps.NonUrgentApplicationMade.Respondent" : ["generalAppNotificationDeadlineDateEn", "generalAppNotificationDeadlineDateCy"]}');

INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.GeneralApps.NonUrgentApplicationMade.Respondent', 'The other parties have requested a change to the case', 'Mae’r partïon eraill wedi gofyn am newid yr achos',
        '<p class="govuk-body">Review their request and respond to it by 4pm on ${generalAppNotificationDeadlineDateEn}. After this date, the application will go to a judge who’ll decide what the next steps will be. <a href={GA_RESPONDENT_INFORMATION_URL} rel="noopener noreferrer" class="govuk-link">Review and respond to the request</a></p>',
        '<p class="govuk-body">Dylech adolygu eu cais ac ymateb iddo erbyn 4pm ar ${generalAppNotificationDeadlineDateCy}. Ar ôl y dyddiad hwn, bydd y cais yn mynd at farnwr a fydd yn penderfynu beth fydd y camau nesaf. <a href={GA_RESPONDENT_INFORMATION_URL} rel="noopener noreferrer" class="govuk-link">Adolygu ac ymateb i’r cais</a></p>',
        'RESPONDENT');

/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.GeneralApps.NonUrgentApplicationUncloaked.Respondent', '{}', '{"Notice.AAA6.GeneralApps.NonUrgentApplicationUncloaked.Respondent" : ["judgeRequestMoreInfoByDateEn", "judgeRequestMoreInfoByDateCy"]}');

INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.GeneralApps.NonUrgentApplicationUncloaked.Respondent', 'The other parties have requested a change to the case', 'Mae’r partïon eraill wedi gofyn am newid yr achos',
        '<p class="govuk-body">Review their request and respond to it by 4pm on ${judgeRequestMoreInfoByDateEn}. After this date, the application will go to a judge who’ll decide what the next steps will be. <a href={GA_RESPONDENT_INFORMATION_URL} rel="noopener noreferrer" class="govuk-link">Review and respond to the request</a></p>',
        '<p class="govuk-body">Dylech adolygu eu cais ac ymateb iddo erbyn 4pm ar ${judgeRequestMoreInfoByDateCy}. Ar ôl y dyddiad hwn, bydd y cais yn mynd at farnwr a fydd yn penderfynu beth fydd y camau nesaf. <a href={GA_RESPONDENT_INFORMATION_URL} rel="noopener noreferrer" class="govuk-link">Adolygu ac ymateb i’r cais</a></p>',
        'RESPONDENT');
