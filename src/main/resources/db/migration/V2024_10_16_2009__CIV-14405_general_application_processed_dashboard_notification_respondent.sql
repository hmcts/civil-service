/**
 * Add scenario
 */

INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.GeneralApps.RespondentResponseSubmitted.Respondent',
        '{"Notice.AAA6.GeneralApps.UrgentApplicationMade.Respondent","Notice.AAA6.GeneralApps.NonUrgentApplicationMade.Respondent","Notice.AAA6.GeneralApps.MoreInfoRequired.Respondent","Notice.AAA6.GeneralApps.OrderMade.Respondent"}',
        '{"Notice.AAA6.GeneralApps.RespondentResponseSubmitted.Respondent": []}');

/**
 * Add notification template
 */

INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.GeneralApps.RespondentResponseSubmitted.Respondent',
        'Application is being processed',
        'Cais yn cael ei brosesu',
        '<p class="govuk-body"> A judge will consider the application. </p>' ||
        '<p class="govuk-body"> You’ll receive an update with information about next steps.</p>',
        '<p class="govuk-body"> Bydd barnwr yn ystyried y cais. </p>' ||
        '<p class="govuk-body">Fe gewch diweddariad gyda gwybodaeth am y camau nesaf.</p>',
        'RESPONDENT');
