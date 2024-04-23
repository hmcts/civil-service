/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimIssue.HWF.InfoRequired', '{"Notice.AAA6.ClaimIssue.HWF.Requested"}',
        '{"Notice.AAA6.ClaimIssue.HWF.InfoRequired" : ["typeOfFee"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.ClaimIssue.HWF.InfoRequired',
        'Your help with fees application needs more information',
        'Your help with fees application needs more information',
        '<p class="govuk-body">We need more information on your application for help with the claim fee.<br>You''ve been sent an email with further details. If you''ve already read the email and taken action, disregard this message.<br>You can pay by phone by calling {civilMoneyClaimsTelephone}.</p>',
        '<p class="govuk-body">We need more information on your application for help with the claim fee.<br>You''ve been sent an email with further details. If you''ve already read the email and taken action, disregard this message.<br>You can pay by phone by calling {civilMoneyClaimsTelephone}.</p>',
        'CLAIMANT');
