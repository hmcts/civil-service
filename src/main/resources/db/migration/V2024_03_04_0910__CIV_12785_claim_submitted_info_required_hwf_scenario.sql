/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimIssue.HWF.InfoRequired', '{"Notice.AAA6.ClaimIssue.HWF.Requested"}',
        '{"Notice.AAA6.ClaimIssue.HWF.InfoRequired" : []}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.ClaimIssue.HWF.InfoRequired',
        'Your help with fees application needs more information',
        'Mae angen i chi ddarparu mwy o wybodaeth am eich cais am help i dalu ffioedd',
        '<p class="govuk-body">We need more information on your application for help with the claim fee. You''ve been sent an email with further details. If you''ve already read the email and taken action, you can disregard this message. You can pay by phone by calling {civilMoneyClaimsTelephone}.</p>',
        '<p class="govuk-body">Mae arnom angen mwy o wybodaeth am eich cais am help i dalu ffi’r hawliad. Anfonwyd e-bost atoch gyda mwy o fanylion. Os ydych eisoes wedi darllen yr e-bost ac wedi gweithredu, gallwch anwybyddu''r neges hon. Gallwch dalu dros y ffôn drwy ffonio {civilMoneyClaimsTelephoneWelshSpeaker}.</p>',
        'CLAIMANT');
