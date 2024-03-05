/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA7.ClaimIssue.HWF.PartRemission', '{"Notice.AAA7.ClaimIssue.HWF.Requested"}',
        '{"Notice.AAA7.ClaimIssue.HWF.PartRemission" : ["claimIssueRemissionAmount","claimIssueOutStandingAmount","claimIssuePaymentDueDate"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA7.ClaimIssue.HWF.PartRemission', 'Your help with fees application has been reviewed',
        'Your help with fees application has been reviewed',
        'You''ll get help with the claim fee. You''ll receive ${claimIssueRemissionAmount} towards it. <br> You must still pay the remaining fee of ${claimIssueOutStandingAmount} by ${claimIssuePaymentDueDate}.You can pay by phone by calling {civilMoneyClaimsTelephone}.',
        'You''ll get help with the claim fee. You''ll receive ${claimIssueRemissionAmount} towards it. <br> You must still pay the remaining fee of ${claimIssueOutStandingAmount} by ${claimIssuePaymentDueDate}.You can pay by phone by calling {civilMoneyClaimsTelephone}.',
        'CLAIMANT');

