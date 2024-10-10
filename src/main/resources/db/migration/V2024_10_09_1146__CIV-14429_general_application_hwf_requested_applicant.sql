/**
 * Add scenario for claimant
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.GeneralApps.HwFRequested.Applicant',
        '{"Notice.AAA6.GeneralApps.ApplicationFeeRequired.Applicant",
        "Notice.AAA6.GeneralApps.AdditionalApplicationFeeRequired.Applicant"}',
        '{"Notice.AAA6.GeneralApps.HwFRequested.Applicant": ["applicationFeeTypeEn", "applicationFeeTypeCy"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.GeneralApps.HwFRequested.Applicant',
        'We’re reviewing your help with fees application',
        'Rydym yn adolygu eich cais am help i dalu ffioedd',
        '<p class="govuk-body">You’ve applied for help with the ${applicationFeeTypeEn} fee. You’ll receive an update in 5 to 10 working days.</p>',
        '<p class="govuk-body">Fe wnaethoch gais am help i dalu’r ffi gwneud ${applicationFeeTypeCy}. Byddwch yn cael diweddariad mewn 5 i 10 diwrnod gwaith.</p>',
        'APPLICANT');
