/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA7.ClaimIssue.HWF.Updated', '{"Notice.AAA7.ClaimIssue.HWF.InvalidRef"}', '{"Notice.AAA7.ClaimIssue.HWF.Updated": ["typeOfFee"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA7.ClaimIssue.HWF.Updated', 'Your help with fees application has been updated', 'Your help with fees application has been updated',
        'You''ve applied for help with the ${typeOfFee} fee. You''ll receive an update from us within 5 to 10 working days.',
        'You''ve applied for help with the ${typeOfFee} fee. You''ll receive an update from us within 5 to 10 working days.',
        'CLAIMANT');
