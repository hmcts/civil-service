/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA7.ClaimIssue.Response.Await', '{"Notice.AAA7.ClaimIssue.ClaimFee.Required"}', '{"Notice.AAA7.ClaimIssue.Response.Await":[]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA7.ClaimIssue.Response.Await', 'Wait for defendant to respond', 'Wait for defendant to respond',
        '${claimantName} has until <Date> to respond. They can request an extra 28 days if they need it.',
        '${claimantName} has until <Date> to respond. They can request an extra 28 days if they need it.',
        'CLAIMANT');

/**
 * Add task list items
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
values ('<a href={VIEW_CLAIM_URL}>View the claim</a>', 'The claim','<a href={VIEW_CLAIM_URL}>View the claim</a>',
        'The claim', 'Claim.View', 'Scenario.AAA7.ClaimIssue.Response.Await', '{3, 3}', 'CLAIMANT', 1),
       ('<a href={VIEW_INFO_ABOUT_CLAIMANT}>View information about the claimant</a>', 'The claim','<a href={VIEW_INFO_ABOUT_CLAIMANT_URL}>View information about the claimant</a>',
        'The claim', 'Claim.Claimant.Info', 'Scenario.AAA7.ClaimIssue.Response.Await', '{3, 3}', 'CLAIMANT', 2),
       ('<a href={VIEW_INFO_ABOUT_DEFENDANT}>View information about the defendant</a>', 'The response','<a href={VIEW_INFO_ABOUT_DEFENDANT_URL}>View information about the defendant</a>',
        'The response', 'Response.Defendant.Info', 'Scenario.AAA7.ClaimIssue.Response.Await', '{3, 3}', 'CLAIMANT', 4),
       ('<a href={VIEW_ORDERS_AND_NOTICES}>View orders and notices</a>', 'Orders and notices from the court' ,'<a href={VIEW_ORDERS_AND_NOTICES_URL}>View orders and notices</a>',
        'Orders and notices from the court', 'Order.View', 'Scenario.AAA7.ClaimIssue.Response.Await', '{3, 3}', 'CLAIMANT', 10);
