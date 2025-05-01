/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.GeneralApps.OtherPartyUploadedDocuments.Applicant', '{}', '{"Notice.AAA6.GeneralApps.OtherPartyUploadedDocuments.Applicant" : []}');

INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.GeneralApps.OtherPartyUploadedDocuments.Applicant', 'The other parties have uploaded documents to the application', 'Mae’r partïon eraill wedi uwchlwytho dogfennau i’r cais',
        '<p class="govuk-body"><a href={GA_VIEW_APPLICATION_URL} rel="noopener noreferrer" class="govuk-link">Review the uploaded documents.</a></p>',
        '<p class="govuk-body"><a href={GA_VIEW_APPLICATION_URL} rel="noopener noreferrer" class="govuk-link">Adolygu’r dogfennau a uwchlwythwyd.</a></p>',
        'APPLICANT');
