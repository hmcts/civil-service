/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA7.ClaimIssue.HWF.Rejected', '{"Notice.AAA7.ClaimIssue.HWF.Requested","Notice.AAA7.ClaimIssue.HWF.Updated","Notice.AAA7.ClaimIssue.HWF.InvalidRef","Notice.AAA7.ClaimIssue.HWF.InfoRequired"}',
        '{"Notice.AAA7.ClaimIssue.HWF.Rejected" : ["claimFee"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA7.ClaimIssue.HWF.Rejected', 'Your help with fees application has been rejected',
        'Your help with fees application has been rejected',
        '<p class="govuk-body">We''ve rejected your application for help with the claim fee. See the email for further details.</p><p class="govuk-body">You''ll need to pay the full fee of ${claimFee}. You can pay by phone by calling {civilMoneyClaimsTelephone}.</p>',
        '<p class="govuk-body">We''ve rejected your application for help with the claim fee. See the email for further details.</p><p class="govuk-body">You''ll need to pay the full fee of ${claimFee}. You can pay by phone by calling {civilMoneyClaimsTelephone}.</p>',
        'CLAIMANT');
