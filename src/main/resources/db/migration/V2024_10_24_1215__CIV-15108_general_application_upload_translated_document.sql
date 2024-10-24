/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.GeneralApps.TranslatedDocumentUploaded.Applicant',
        '{}', '{"Notice.AAA6.GeneralApps.TranslatedDocumentUploaded.Applicant": []}'),
       ('Scenario.AAA6.GeneralApps.TranslatedDocumentUploaded.Respondent',
        '{}', '{"Notice.AAA6.GeneralApps.TranslatedDocumentUploaded.Respondent": []}');

INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.GeneralApps.TranslatedDocumentUploaded.Applicant',
        'Translated document for the application is now available',
        'Mae’r ddogfen a gyfieithwyd ar gyfer y cais bellach ar gael',
        '<p class="govuk-body"><a href="{GA_RESPONSE_VIEW_APPLICATION_URL}" class="govuk-link">View translated application documents.</a></p>',
        '<p class="govuk-body"><a href="{GA_RESPONSE_VIEW_APPLICATION_URL}" class="govuk-link">Gweld dogfennau’r cais a gyfieithwyd.</a></p>',
        'APPLICANT'),
       ('Notice.AAA6.GeneralApps.TranslatedDocumentUploaded.Respondent',
        'Translated document for the application is now available',
        'Mae’r ddogfen a gyfieithwyd ar gyfer y cais bellach ar gael',
        '<p class="govuk-body"><a href="{GA_RESPONSE_VIEW_APPLICATION_URL}" class="govuk-link">View translated application documents.</a></p>',
        '<p class="govuk-body"><a href="{GA_RESPONSE_VIEW_APPLICATION_URL}" class="govuk-link">Gweld dogfennau’r cais a gyfieithwyd.</a></p>',
        'RESPONDENT');
