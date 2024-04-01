/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimIssue.HWF.InvalidRef', '{"Notice.AAA6.ClaimIssue.HWF.Requested"}',
        '{"Notice.AAA6.ClaimIssue.HWF.InvalidRef" : ["typeOfFee"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.ClaimIssue.HWF.InvalidRef',
        'You''ve provided an invalid help with fees reference number',
        'You''ve provided an invalid help with fees reference number',
        '<p class="govuk-body">You''ve applied for help with the ${typeOfFee} fee, but the reference number is invalid.<br>You''ve been sent an email with instructions on what to do next. If you''ve already read the email and taken action, disregard this message.<br>You can pay by phone by calling {civilMoneyClaimsTelephone}.</p>',
        '<p class="govuk-body">You''ve applied for help with the ${typeOfFee} fee, but the reference number is invalid.<br>You''ve been sent an email with instructions on what to do next. If you''ve already read the email and taken action, disregard this message.<br>You can pay by phone by calling {civilMoneyClaimsTelephone}.</p>',
           'CLAIMANT');
