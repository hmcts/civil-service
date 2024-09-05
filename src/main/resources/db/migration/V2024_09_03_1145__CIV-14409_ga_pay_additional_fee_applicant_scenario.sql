/**
 * Add scenario for claimant
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.GeneralApps.AdditionalApplicationFeeRequired.Applicant',
        '{"Notice.AAA6.GeneralApps.ApplicationSubmitted.Applicant", "Notice.AAA6.GeneralApps.HwF.FeePaid.Applicant", "Notice.AAA6.GeneralApps.HwF.FullRemission.Applicant"}',
        '{"Notice.AAA6.GeneralApps.AdditionalApplicationFeeRequired.Applicant": []}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.GeneralApps.AdditionalApplicationFeeRequired.Applicant',
        'You must pay an additional application fee',
        'Rhaid i chi dalu ffi gwneud cais ychwanegol',
        '<p class="govuk-body">The court requires you to pay an additional fee before your application can progress further.</p><p class="govuk-body"><a href="{GA_VIEW_APPLICATION_URL}" class="govuk-link">Pay the additional application fee.</a></p>',
        '<p class="govuk-body">Mae''r llys angen i chi dalu ffi ychwanegol cyn y gall eich cais gael ei brosesu ymhellach.</p><p class="govuk-body"><a href="{GA_VIEW_APPLICATION_URL}" class="govuk-link">Talu ffi’r gwneud cais ychwanegol.</a></p>',
        'APPLICANT');
