/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.GeneralApps.ApplicationFeeRequired.Applicant', '{}', '{"Notice.AAA6.GeneralApps.ApplicationFeeRequired.Applicant" : ["applicationFee"]}');

INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.GeneralApps.ApplicationFeeRequired.Applicant', 'Pay application fee', 'Talu ffi gwneud cais',
        '<p class="govuk-body">To finish making your application, you must pay the application fee of ${applicationFee} as soon as possible. Your application will be paused and will not be sent to the other parties or considered by a judge until you’ve paid the fee. <a href={GA_VIEW_APPLICATION_URL} rel="noopener noreferrer" class="govuk-link">Pay application fee.</a></p>',
        '<p class="govuk-body">I orffen gwneud eich cais, rhaid i chi dalu’r ffi gwneud cais o ${applicationFee} cyn gynted â phosib. Bydd eich cais yn cael ei oedi ac ni chaiff ei anfon at y partïon eraill na’i ystyried gan farnwr nes i chi dalu’r ffi. <a href={GA_VIEW_APPLICATION_URL} rel="noopener noreferrer" class="govuk-link">Talu’r ffi gwneud cais.</a></p>',
        'APPLICANT');
