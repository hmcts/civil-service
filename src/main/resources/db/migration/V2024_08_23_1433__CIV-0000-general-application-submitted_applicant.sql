/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.GeneralApplication.Submitted', '{"Notice.AAA6.GeneralApplication.Fee.Required"}', '{"Notice.AAA6.GeneralApplication.submitted" : []}');

INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.GeneralApplication.submitted', 'Your application has been submitted', 'Your application has been submitted',
        '<p class="govuk-body">Your Application has been submitted.</p>',
        '<p class="govuk-body">Your Application has been submitted.</p>',
        'CLAIMANT');
