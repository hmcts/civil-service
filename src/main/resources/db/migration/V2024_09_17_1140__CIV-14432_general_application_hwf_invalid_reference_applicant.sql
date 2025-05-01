/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.GeneralApps.HwF.InvalidRef.Applicant',
        '{"Notice.AAA6.GeneralApps.HwFRequested.Applicant"}',
        '{"Notice.AAA6.GeneralApps.HwF.InvalidRef.Applicant" : ["applicationFeeTypeEn", "applicationFeeTypeCy"]}');

INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.GeneralApps.HwF.InvalidRef.Applicant',
        'You''ve provided an invalid help with fees reference number',
        'Rydych wedi darparu cyfeirnod help i dalu ffioedd annilys',
        '<p class="govuk-body">You''ve applied for help with the ${applicationFeeTypeEn} fee, but the reference number is invalid.</p><p class="govuk-body">You''ve been sent an email with instructions on what to do next. If you''ve already read the email and taken action, you can disregard this message.</p><p class="govuk-body">You can pay by phone by calling {civilMoneyClaimsTelephone}.</p>',
        '<p class="govuk-body">Rydych wedi gwneud cais am help i dalu''r ffi gwneud ${applicationFeeTypeCy}, ond mae''r cyfeirnod yn annilys.</p><p class="govuk-body">Anfonwyd e-bost atoch gyda chyfarwyddiadau ar beth i''w wneud nesaf. Os ydych wedi darllen yr e-bost yn barod ac wedi gweithredu, gallwch anwybyddu''r neges hon.</p><p class="govuk-body">Gallwch dalu dros y ffôn drwy ffonio {civilMoneyClaimsTelephone}.</p>',
        'APPLICANT');
