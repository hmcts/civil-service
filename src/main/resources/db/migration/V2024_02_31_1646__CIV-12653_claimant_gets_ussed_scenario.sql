/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA7.ClaimIssue.Response.Await', '{"Notice.AAA7.ClaimIssue.ClaimFee.Required" : []}', '{"Notice.AAA7.ClaimIssue.Response.Await" : []}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA7.ClaimIssue.Response.Await', 'Wait for defendant to respond', 'Wait for defendant to respond',
        '${claimantName} has until <Date> to respond. They can request an extra 28 days if they need it.',
        '${claimantName} has until <Date> to respond. They can request an extra 28 days if they need it.',
        'CLAIMANT');
