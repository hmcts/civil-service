/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimIssue.HWF.PartRemission', '{"Notice.AAA6.ClaimIssue.HWF.Requested"}',
        '{"Notice.AAA6.ClaimIssue.HWF.PartRemission" : ["claimIssueRemissionAmount","claimIssueOutStandingAmount"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA6.ClaimIssue.HWF.PartRemission',
        'Your help with fees application has been reviewed',
        'Your help with fees application has been reviewed',
        '<p class="govuk-body">You''ll get help with the claim fee. You''ll receive ${claimIssueRemissionAmount} towards it.<br>You must still pay the remaining fee of ${claimIssueOutStandingAmount}. You can pay by phone by calling {civilMoneyClaimsTelephone}.</p>',
        '<p class="govuk-body">You''ll get help with the claim fee. You''ll receive ${claimIssueRemissionAmount} towards it.<br>You must still pay the remaining fee of ${claimIssueOutStandingAmount}. You can pay by phone by calling {civilMoneyClaimsTelephone}.</p>',
        'CLAIMANT');

