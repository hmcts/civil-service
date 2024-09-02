/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.GeneralApplication.Issue.Payment.Required.Applicant', '{}', '{"Notice.AAA6.GeneralApplication.Fee.Required.Applicant" : ["applicationFee"]}');

INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.GeneralApplication.Fee.Required.Applicant', 'You need to pay your Application fee', 'You need to pay your Application fee',
        '<p class="govuk-body">Your Application has not yet been issued, in order to proceed you must pay the application fee of ${applicationFee}. <a href={GENERAL_APPLICATION_FEE_URL} rel="noopener noreferrer" class="govuk-link">Pay the application fee</a></p>',
        '<p class="govuk-body">Your Application has not yet been issued, in order to proceed you must pay the application fee of ${applicationFee}. <a href={GENERAL_APPLICATION_FEE_URL} rel="noopener noreferrer" class="govuk-link">Pay the application fee</a></p>',
        'APPLICANT');
