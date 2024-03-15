/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA7.ClaimIssue.HWF.FullRemission',
        '{"Notice.AAA7.ClaimIssue.HWF.Requested", "Notice.AAA7.ClaimIssue.HWF.InvalidRef", "Notice.AAA7.ClaimIssue.HWF.InfoRequired", "Notice.AAA7.ClaimIssue.HWF.Updated" }',
        '{"Notice.AAA7.ClaimIssue.HWF.FullRemission": ["claimFee"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA7.ClaimIssue.HWF.FullRemission', 'Your help with fees application has been reviewed', 'Your help with fees application has been reviewed',
        'The full claim fee of ${claimFee} will be covered. You do not need to make a payment.',
        'The full claim fee of ${claimFee} will be covered. You do not need to make a payment.',
        'CLAIMANT');
