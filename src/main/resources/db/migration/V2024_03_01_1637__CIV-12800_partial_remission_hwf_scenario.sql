/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA7.Notice.ClaimIssue.HWF.PartRemission', '{"Notice.AAA7.ClaimIssue.HWF.Requested"}',
        '{"Notice.AAA7.ClaimIssue.HWF.PartRemission" : ["remissionAmount","outStandingAmount","paymentDueDate"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates ( template_name, title_En, title_Cy, description_En, description_Cy
                                                  , notification_role)
VALUES ('Notice.AAA7.ClaimIssue.HWF.PartRemission', 'Your help with fees application has been reviewed',
        'Your help with fees application has been reviewed',
        'You''ll get help with the claim fee. You''ll receive ${remissionAmount} towards it.You must still pay the remaining fee of ${outStandingAmount} by ${paymentDueDate}. You can pay by phone by calling 0300 123 7050.',
        'You''ll get help with the claim fee. You''ll receive ${remissionAmount} towards it.You must still pay the remaining fee of  ${outStandingAmount} by ${paymentDueDate}. You can pay by phone by calling 0300 123 7050.',
        'CLAIMANT');

