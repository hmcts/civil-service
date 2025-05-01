/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimIssue.HWF.InvalidRef', '{"Notice.AAA6.ClaimIssue.HWF.Requested"}',
        '{"Notice.AAA6.ClaimIssue.HWF.InvalidRef" : []}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.ClaimIssue.HWF.InvalidRef',
        'You''ve provided an invalid help with fees reference number',
        'Rydych wedi darparu cyfeirnod help i dalu ffioedd annilys',
        '<p class="govuk-body">You''ve applied for help with the claim fee, but the reference number is invalid. You''ve been sent an email with instructions on what to do next. If you''ve already read the email and taken action, you can disregard this message. You can pay by phone by calling {civilMoneyClaimsTelephone}.</p>',
        '<p class="govuk-body">Rydych wedi gwneud cais am help i dalu ffi’r hawliad, ond mae''r cyfeirnod yn annilys. Anfonwyd e-bost atoch gyda chyfarwyddiadau ar beth i''w wneud nesaf. Os ydych eisoes wedi darllen yr e-bost ac wedi gweithredu, gallwch anwybyddu''r neges hon. Gallwch dalu dros y ffôn drwy ffonio {civilMoneyClaimsTelephoneWelshSpeaker}.</p>',
           'CLAIMANT');
